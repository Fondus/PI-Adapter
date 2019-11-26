package tw.fondus.fews.adapter.pi.irrigation.nchc;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import org.joda.time.DateTime;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink.v3.entity.data.RawData;
import tw.fondus.commons.json.senslink.v3.util.SensLinkUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.SensLinkArgument;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.ModelUtils;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The model senslink-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use to create model parameters or input from the senslink, copy files.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeSensLinkAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		SensLinkArgument argument = new SensLinkArgument();
		new IrrigationOptimizeSensLinkAdapter().execute( args, argument );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		SensLinkArgument modelArguments = (SensLinkArgument) arguments;

		Path executablePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC Irrigation-Optimize ParameterAdapter: The executable folder is not exist." );

		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();
		String host = SensLinkUtils.URL_WRA;

		try {
			// Login SensLink 3.0 by OAuth 2.0
			Optional<String> optToken = SensLinkUtils.getAccessToken(
					username, password,
					Strman.append( host, SensLinkUtils.URL_OAUTH_TOKEN ) );

			OptionalUtils.ifPresentOrElse( optToken, token -> {
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: The SensLink 3.0 system login successfully." );
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Try to get the water requirement data from the SensLink 3.0 system." );

				int duration = modelArguments.getDuration();
				List<String> waterRequirementTargets = modelArguments.getWaterRequirementTargets();
				List<String> waterRequirementsFiles = modelArguments.getWaterRequirementsFiles();
				this.createWaterRequirements( logger, inputPath, executablePath, host, token, duration, waterRequirementTargets, waterRequirementsFiles );

				if ( modelArguments.isPullFromSensLinkFlag() ){
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start pull the model input information from the SensLink system." );

					List<String> ids = modelArguments.getSensLinkIds();
					this.createModelInput( logger, modelArguments, inputPath, host, token, ids );
				}

			},
			() -> logger.log( LogLevel.WARN, "NCHC Irrigation-Optimize SensLinkAdapter: SensLink System Login failed.") );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize SensLinkAdapter: Adapter connect to external system has IO problem." );
		}
	}

	/**
	 * Create model input PI-XML from the SensLink System.
	 *
	 * @param logger
	 * @param modelArguments
	 * @param inputPath
	 * @param host
	 * @param token
	 * @param ids
	 */
	private void createModelInput(
			PiDiagnosticsLogger logger, SensLinkArgument modelArguments,
			Path inputPath,
			String host, String token,
			List<String> ids ){
		if ( ids.size() != 3 ){
			logger.log( LogLevel.WARN, "NCHC Irrigation-Optimize SensLinkAdapter: The senslink ids length should be 3." );
		} else {
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start pull the data by senslink ids." );

			String locationId = modelArguments.getInputs().get( 0 );

			Optional<RawData> opt1 = SensLinkUtils.readLatestByPQId( host, token, ids.get( 0 ), 0 );
			Optional<RawData> opt2 = SensLinkUtils.readLatestByPQId( host, token, ids.get( 1 ), 0 );
			Optional<RawData> opt3 = SensLinkUtils.readLatestByPQId( host, token, ids.get( 2 ), 0 );

			OptionalUtils.ifPresentOrElse( opt1, opt2, opt3, ( data1, data2, data3 ) -> {
				SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();

				DateTime startTime = data1.getTime();
				TimeSeriesUtils.fillPiTimeSeriesHeaderIrregular( handler, locationId, modelArguments.getParameter(), modelArguments.getUnit() );
				this.fillHandlerValue( handler, 0, 10, startTime, data1 );
				this.fillHandlerValue( handler, 10, 20, startTime, data2 );
				this.fillHandlerValue( handler, 20, 30, startTime, data3 );

				try {
					TimeSeriesLightUtils.writePIFile( handler, inputPath.resolve( modelArguments.getOutputs().get( 0 ) ).toString() );
				} catch (InterruptedException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize SensLinkAdapter: Write the PI-XML has something wrong!" );
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize SensLinkAdapter: Write the PI-XML has something wrong!" );
				}
			},
			() -> logger.log( LogLevel.WARN, "NCHC Irrigation-Optimize SensLinkAdapter: Pull the senslink data is empty!" ));
		}
	}

	/**
	 * Fill the XML handler value.
	 *
	 * @param handler
	 * @param start
	 * @param end
	 * @param time
	 * @param data
	 */
	private void fillHandlerValue( SimpleTimeSeriesContentHandler handler,
			int start, int end, DateTime time, RawData data ){
		IntStream.range( start, end ).forEach( i -> {
			TimeSeriesLightUtils.addPiTimeSeriesValue( handler,
					time.plus( i * 86400000L ).getMillis(), data.getValue().floatValue() );
		} );
	}

	/**
	 * Create the model water requirement files from the senslink system.
	 *
	 * @param logger
	 * @param inputPath
	 * @param executablePath
	 * @param host
	 * @param token
	 * @param duration
	 * @param targets
	 * @param files
	 */
	private void createWaterRequirements(
			PiDiagnosticsLogger logger,
			Path inputPath, Path executablePath,
			String host, String token, int duration,
			List<String> targets, List<String> files ){
		logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start create the water requirement files." );
		IntStream.range( 0, targets.size() ).forEach( i -> {
			SensLinkUtils.readLatestByPQId( host, token, targets.get( i ), 0 ).ifPresent( data -> {
				String fileName = files.get( i );
				String content = ModelUtils.createDurationContent( data.getValue(), duration );

				try {
					ModelUtils.writeFile( inputPath, executablePath, fileName, content );
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize SensLinkAdapter: Create the water requirement file: {} has IO problem.", fileName );
				}
			} );
		} );
	}
}
