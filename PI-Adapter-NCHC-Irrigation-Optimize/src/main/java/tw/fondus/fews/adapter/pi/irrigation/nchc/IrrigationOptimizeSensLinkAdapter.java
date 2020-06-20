package tw.fondus.fews.adapter.pi.irrigation.nchc;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.oauth2.model.OAuthToken;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3Runtime;
import tw.fondus.commons.rest.senslink.v3.model.record.Record;
import tw.fondus.commons.rest.senslink.v3.model.record.RecordTimeSeries;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Host;
import tw.fondus.commons.rest.senslink.v3.util.TimeZone;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.SensLinkArgument;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.ModelUtils;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model senslink-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use to create model parameters or input from the senslink, copy files.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeSensLinkAdapter extends PiCommandLineExecute {
	private final SensLinkApiV3 api;

	public IrrigationOptimizeSensLinkAdapter(){
		this.api = SensLinkApiV3Runtime.DEFAULT;
	}

	public static void main( String[] args ) {
		SensLinkArgument argument = SensLinkArgument.instance();
		new IrrigationOptimizeSensLinkAdapter().execute( args, argument );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		SensLinkArgument modelArguments = this.asArguments( arguments, SensLinkArgument.class );

		Path executablePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC Irrigation-Optimize ParameterAdapter: The executable folder is not exist." );

		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();

		try {
			// Login SensLink 3.0 by OAuth 2.0
			Optional<OAuthToken> optional = this.api.getAccessToken( username, password, SensLinkApiV3Host.IOW );
			optional.ifPresentOrElse( token -> {
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: The SensLink 3.0 system login successfully." );
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Try to get the water requirement data from the SensLink 3.0 system." );

				int duration = modelArguments.getDuration();
				String baseId = modelArguments.getWaterRequirementTimeBase();
				List<String> waterRequirementTargets = modelArguments.getWaterRequirementTargets();
				List<String> waterRequirementsFiles = modelArguments.getWaterRequirementsFiles();
				this.createWaterRequirements( inputPath, executablePath, token.getAccess(), baseId, duration, waterRequirementTargets, waterRequirementsFiles );

				if ( modelArguments.isPullFromSensLinkFlag() ){
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start pull the model input information from the SensLink system." );

					List<String> ids = modelArguments.getSensLinkIds();
					this.createModelInput( modelArguments, inputPath, token.getAccess(), ids );
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
	 * @param modelArguments arguments
	 * @param inputPath input path
	 * @param token access token
	 * @param ids location ids
	 */
	private void createModelInput(
			SensLinkArgument modelArguments,
			Path inputPath,
			String token,
			List<String> ids ){
		if ( ids.size() != 3 ){
			this.getLogger().log( LogLevel.WARN, "NCHC Irrigation-Optimize SensLinkAdapter: The senslink ids length should be 3." );
		} else {
			this.getLogger().log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start pull the data by senslink ids." );

			String locationId = modelArguments.getInputs().get( 0 );

			Record record1 = this.api.readLatestDataByPQ( token, ids.get( 0 ), TimeZone.UTC0 );
			Record record2 = this.api.readLatestDataByPQ( token, ids.get( 1 ), TimeZone.UTC0 );
			Record record3 = this.api.readLatestDataByPQ( token, ids.get( 2 ), TimeZone.UTC0 );
			SimpleTimeSeriesContentHandler handler = TimeSeriesLightUtils.seriesHandler();
			DateTime startTime = record1.getTime();
			TimeSeriesUtils.addHeaderIrregular( handler, locationId, modelArguments.getParameter(), modelArguments.getUnit() );
			this.fillHandlerDayValue( handler, 0, 10, startTime, record1 );
			this.fillHandlerDayValue( handler, 10, 20, startTime, record2 );
			this.fillHandlerDayValue( handler, 20, 30, startTime, record3 );

			try {
				TimeSeriesLightUtils.write( handler, inputPath.resolve( modelArguments.getOutputs().get( 0 ) ) );
			} catch (IOException e) {
				this.getLogger().log( LogLevel.ERROR, "NCHC Irrigation-Optimize SensLinkAdapter: Write the PI-XML has something wrong!" );
			}
		}
	}

	/**
	 * Fill the XML handler value of day duration.
	 *
	 * @param handler series handler
	 * @param start start
	 * @param end end
	 * @param time time
	 * @param record record
	 */
	private void fillHandlerDayValue( SimpleTimeSeriesContentHandler handler,
			int start, int end, DateTime time, Record record ){
		IntStream.range( start, end ).forEach( i -> TimeSeriesLightUtils.addValue( handler,
				time.plus( i * 86400000L ).getMillis(), record.asNumber() ) );
	}

	/**
	 * Create the model water requirement files from the senslink system.
	 *
	 * @param inputPath input path
	 * @param executablePath executable path
	 * @param token access token
	 * @param id id
	 * @param duration duration
	 * @param targets targets
	 * @param files files
	 */
	private void createWaterRequirements(
			Path inputPath, Path executablePath,
			String token, String id,
			int duration, List<String> targets, List<String> files ){
		this.getLogger().log( LogLevel.INFO, "NCHC Irrigation-Optimize SensLinkAdapter: Start create the water requirement files." );
		IntStream.range( 0, targets.size() ).forEach( i -> {
			if ( duration == 24 ){
				DateTime currentTime = new DateTime();
				RecordTimeSeries series = this.api.readTimeSeries( token, targets.get( i ), currentTime, currentTime.plusHours( duration - 1 ), false, TimeZone.UTC0 );

				String fileName = files.get( i );
				String content = IntStream.range( 0, duration )
						.mapToObj( j -> series.size() > j ? series.get( j ).getValue().toString() : BigDecimal.ZERO.toString() )
						.collect( Collectors.joining( Strings.SPLIT_TAB ) );

				ModelUtils.writeFile( inputPath, executablePath, fileName, content );
			} else {
				// Get the base time from the SensLink system.
				DateTime baseTime = this.api.readLatestDataByPQ( token, id, TimeZone.UTC0 ).getTime();

				RecordTimeSeries series = this.api.readTimeSeries( token, targets.get( i ), baseTime, baseTime.plusDays( duration - 1 ), false, TimeZone.UTC0 );
				List<Record> records = series.getCollection();
				// Get the mapping value from time series
				BigDecimal firstTenDay = this.getWaterRequirementValue( records, 0 );
				BigDecimal secondTenDay = this.getWaterRequirementValue( records, 11 );
				BigDecimal threeTenDay = this.getWaterRequirementValue( records, 21 );

				// Create the model input content.
				String firstTenDayContent = ModelUtils.createDurationContent( firstTenDay, 10 );
				String secondTenDayContent = ModelUtils.createDurationContent( secondTenDay, 10 );
				String threeTenDayContent = ModelUtils.createDurationContent( threeTenDay, 10 );

				String fileName = files.get( i );
				String content = String.join( Strings.SPLIT_TAB, firstTenDayContent, secondTenDayContent, threeTenDayContent );
				ModelUtils.writeFile( inputPath, executablePath, fileName, content );
			}
		} );
	}

	/**
	 * Get the water requirement with specific index from the SensLink time series.
	 *
	 * @param records records
	 * @param target target limit index
	 * @return value
	 */
	private BigDecimal getWaterRequirementValue( List<Record> records, int target ){
		if ( records.size() <= target ){
			return BigDecimal.ZERO;
		} else {
			return records.get( target ).asNumber();
		}
	}
}
