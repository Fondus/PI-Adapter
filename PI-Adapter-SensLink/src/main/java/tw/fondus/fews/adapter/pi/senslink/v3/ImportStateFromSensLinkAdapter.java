package tw.fondus.fews.adapter.pi.senslink.v3;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink.v3.entity.data.TimeSeries;
import tw.fondus.commons.json.senslink.v3.entity.data.TimeStateSeries;
import tw.fondus.commons.json.senslink.v3.util.SensLinkUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Model adapter for import state data from the SensLink 3.0 with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ImportStateFromSensLinkAdapter extends PiCommandLineExecute {

	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new ImportStateFromSensLinkAdapter().execute(args, arguments);
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;

		Path inputXML = Prevalidated.checkExists(
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0)),
				"SensLink 3.0 Import State Adapter: The input XML not exists!" );

		try {
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			logger.log( LogLevel.INFO, "SensLink 3.0 Import State Adapter: Start import {} datas from the SensLink System.", String.valueOf( locationIds.size() ) );

			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();
			String host = SensLinkUtils.URL_WRA;

			// Login SensLink 3.0 by OAuth 2.0
			Optional<String> optToken = SensLinkUtils.getAccessToken(
					username,
					password,
					Strman.append( host, SensLinkUtils.URL_OAUTH_TOKEN ));

			OptionalUtils.ifPresentOrElse( optToken, token -> {
						logger.log( LogLevel.INFO, "SensLink 3.0 Import State Adapter: The SensLink 3.0 system login successfully, try to get data from the SensLink 3.0 system.");

						DateTime start = timeZero.minusDays( modelArguments.getDuration() );
						String startString = TimeUtils.toString( start, TimeUtils.YMDTHMS_DOT, TimeUtils.UTC0 );
						String endString = TimeUtils.toString( timeZero, TimeUtils.YMDTHMS_DOT, TimeUtils.UTC0 );

						List<TimeStateSeries> datas = new ArrayList<>();
						locationIds.forEach( locationId -> {
							SensLinkUtils.readTimeStateSeries( host, token, locationId, startString, endString, true ).ifPresent( series -> {
								datas.add( series );
							} );
						} );

						if ( datas.size() > 0 ){
							logger.log( LogLevel.INFO, "SensLink 3.0 Import State Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML.");

							try {
								SimpleTimeSeriesContentHandler contentHandler = SensLinkUtils.toTimeStateSeriesArraysIrregular( datas,
										modelArguments.getParameter(), modelArguments.getUnit() );
								TimeSeriesUtils.writePIFile( contentHandler,
										Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get( 0 ) ) );
							} catch (InterruptedException | IOException e) {
								logger.log( LogLevel.ERROR, "SensLink 3.0 Import State Adapter: adapter write PI-XML has something wrong!.");
							}

						} else {
							logger.log( LogLevel.WARN, "SensLink 3.0 Import State Adapter: Nothing datas from the  SensLink System.");
						}
			},
			() -> logger.log( LogLevel.WARN, "SensLink 3.0 Import State Adapter: SensLink System Login failed.") );

		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Import State Adapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Import State Adapter: Read XML or write the time meta-information has something failed!" );
		}
	}
}
