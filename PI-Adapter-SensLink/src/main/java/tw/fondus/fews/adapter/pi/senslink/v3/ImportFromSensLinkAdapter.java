package tw.fondus.fews.adapter.pi.senslink.v3;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.oauth2.model.OAuthToken;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3Runtime;
import tw.fondus.commons.rest.senslink.v3.model.record.RecordTimeSeries;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Host;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Utils;
import tw.fondus.commons.rest.senslink.v3.util.TimeZone;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Model adapter for import data from the SensLink 3.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ImportFromSensLinkAdapter extends PiCommandLineExecute {
	
	public static void main(String[] args) {
		RunArguments arguments = RunArguments.instance();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );
		
		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"SensLink 3.0 Import Adapter: The input XML not exists!" );
		
		try {
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			logger.log( LogLevel.INFO, "SensLink 3.0 Import Adapter: Try to import {} records from the SensLink System.", locationIds.size() );
			
			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();

			SensLinkApiV3 api = SensLinkApiV3Runtime.DEFAULT;

			try {
				// Login SensLink 3.0 by OAuth 2.0
				Optional<OAuthToken> optional = api.getAccessToken( username, password, SensLinkApiV3Host.IOW );
				optional.ifPresentOrElse( token -> {
					logger.log( LogLevel.INFO,
							"SensLink 3.0 Import Adapter: The SensLink 3.0 system login successfully, try to get records from the SensLink 3.0 system." );

					DateTime start = timeZero.minusDays( modelArguments.getDuration() );
					List<RecordTimeSeries> records = locationIds.stream()
							.map( locationId -> api.readTimeSeries( token.getAccess(), locationId, start, timeZero, true, TimeZone.UTC0 ) )
							.collect( Collectors.toList() );

					if ( records.size() > 0 ) {
						logger.log( LogLevel.INFO,
								"SensLink 3.0 Import Adapter: Start translate SensLink PhysicalQuantity records to PI-XML." );

						TimeSeriesArrays outputArrays = SensLinkApiV3Utils.fromRecordTimeSeries( records, modelArguments.getParameter(), modelArguments.getUnit() );
						try {
							TimeSeriesUtils.write( outputArrays, outputPath.resolve( modelArguments.getOutputs().get( 0 ) ) );
						} catch (IOException e) {
							logger.log( LogLevel.ERROR,
									"SensLink 2.0 Import Adapter: Write the PI-XML has something wrong." );
						}
					} else {
						logger.log( LogLevel.WARN, "SensLink 3.0 Import Adapter: Not receive any records from SensLink." );
					}

				}, () -> logger.log( LogLevel.WARN, "SensLink 3.0 Import Adapter: SensLink System Login failed." ) );
				logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Finished Adapter process." );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "SensLink 3.0 Import Adapter: SensLink System Login failed!" );
			}
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Import Adapter: No time series found in file in the model input files!" );
		}
	}
}
