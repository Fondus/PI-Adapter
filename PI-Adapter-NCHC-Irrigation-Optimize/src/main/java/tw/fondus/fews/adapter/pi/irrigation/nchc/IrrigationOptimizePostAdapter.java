package tw.fondus.fews.adapter.pi.irrigation.nchc;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.PostArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The model post-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use turn model main output to PI-XML.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class IrrigationOptimizePostAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PostArguments arguments = PostArguments.instance();
		new IrrigationOptimizePostAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PostArguments modelArguments = this.asArguments( arguments, PostArguments.class );

		Path modelOutputMain = Prevalidated.checkExists(
				outputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"NCHC Irrigation-Optimize PostAdapter: The mode output file not exist." );

		Path modelOutputSub = Prevalidated.checkExists(
				outputPath.resolve( modelArguments.getInputs().get( 1 ) ),
				"NCHC Irrigation-Optimize PostAdapter: The mode output file not exist." );

		Path modelOutputThree = Prevalidated.checkExists(
				outputPath.resolve( modelArguments.getInputs().get( 2 ) ),
				"NCHC Irrigation-Optimize PostAdapter: The mode output file not exist." );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 3 ) ),
				"NCHC Irrigation-Optimize PostAdapter: The XML file is not exist." );

		try {
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Read model start time." );
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			DateTime startTime = new DateTime( timeSeriesArrays.getPeriod().getStartTime() );
			long duration = modelArguments.getDuration();

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Read model main output file content." );
			Map<String, List<BigDecimal>> outputMap = this.readModelOutput( modelOutputMain );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Read model sub output file content." );
			this.readSubModelOutput( outputMap, modelOutputSub, modelArguments.getSubLocationId() );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Read model three output file content." );
			this.readThreeModelOutput( outputMap, modelOutputThree, modelArguments.getThreeLocationIds() );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Create PI-XML content from the model output." );
			SimpleTimeSeriesContentHandler handler = TimeSeriesLightUtils.seriesHandler();
			outputMap.forEach( (id, data) -> {
				if ( duration == 86400000L ){
					TimeSeriesUtils.addHeaderIrregular( handler, id, modelArguments.getParameter(), modelArguments.getUnit() );
				} else {
					TimeSeriesLightUtils.addHeader( handler, id, modelArguments.getParameter(), modelArguments.getUnit(), duration );
				}

				IntStream.range( 0, data.size() ).forEach( i ->
					TimeSeriesLightUtils.addValue( handler, startTime.plus( i * duration ).getMillis(), data.get( i ) )
				);
			} );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Write the PI-XML." );
			Path outputXML = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
			TimeSeriesLightUtils.write( handler, outputXML );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PostAdapter: Finished PostAdapter." );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "No time series found in file in the PI-XML!" );
		}
	}

	/**
	 * Read the model output to a map, key is id.
	 *
	 * @param modelOutput model output path
	 * @return map used to store model result
	 */
	private Map<String, List<BigDecimal>> readModelOutput( Path modelOutput ) {
		return PathReader.readAllLines( modelOutput )
				.stream()
				.map( line -> line.trim().split( Strings.SPLIT_SPACE_MULTIPLE ) )
				.collect( Collectors.toMap(
						temps -> temps[0],
						temps -> IntStream.range( 1, temps.length ).mapToObj( i -> NumberUtils.create( temps[i] ) ).collect( Collectors.toList() )
				) );
	}

	/**
	 * Read the model sub output and insert to map, key is locationId.
	 *
	 * @param outputMap map used to store model result
	 * @param modelOutput model output path
	 * @param locationId location id
	 */
	private void readSubModelOutput( Map<String, List<BigDecimal>> outputMap, Path modelOutput, String locationId ) {
		List<String> lines = PathReader.readAllLines( modelOutput );
		List<BigDecimal> values = lines.subList( 2, lines.size() )
				.stream()
				.map( line -> line.trim().split( Strings.SPLIT_SPACE_MULTIPLE ) )
				.map( temps -> NumberUtils.create( temps[temps.length - 1] ) )
				.collect( Collectors.toList() );
		outputMap.putIfAbsent( locationId, values );
	}

	/**
	 * Read the model three output and insert to map, key is locationId.
	 *
	 * @param outputMap map used to store model result
	 * @param modelOutput model output path
	 * @param locationIds location ids
	 */
	private void readThreeModelOutput( Map<String, List<BigDecimal>> outputMap, Path modelOutput, List<String> locationIds ) {
		List<String> lines = PathReader.readAllLines( modelOutput );
		IntStream.range( 0, lines.size() ).forEach( i -> {
			String locationId = locationIds.get( i );
			String line = lines.get( i );
			List<BigDecimal> values = Stream.of( line.trim().split( Strings.SPLIT_SPACE_MULTIPLE ) )
					.map( NumberUtils::create )
					.collect( Collectors.toList() );
			outputMap.putIfAbsent( locationId, values.subList( 1, values.size() ) );
		} );
	}
}
