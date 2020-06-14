package tw.fondus.fews.adapter.pi.example;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The Model pre-adapter for running example model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ExamplePreAdapter extends PiCommandLineExecute {
	private static final String TIME_FORMAT = "yyyyMMddHHmm";
	private static final String TXT = ".txt";
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new ExamplePreAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = this.asIOArguments( arguments );
		
		try {
			Path inputXML = Prevalidated.checkExists(
					inputPath.resolve( modelArguments.getInputs().get(0) ),
					"Example PreAdapter: The input XML do not exists!" );
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			logger.log( LogLevel.INFO, "Example PreAdapter: Start create model input files.");
			
			TimeSeriesArray array = timeSeriesArrays.get( 0 );
			String modelInput = this.createModelInputContent( array );
			FileUtils.writeText( inputPath.resolve( array.getHeader().getLocationId() + TXT ).toString(), modelInput );
			
			logger.log( LogLevel.INFO, "Example PreAdapter: Finished create model input files.");
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "Example PreAdapter: The adapter has something wrong!" );
		}
	}
	
	/**
	 * Mapping timeSeriesArray -> model input content logic.
	 * 
	 * @param array array
	 * @return model input content
	 */
	private String createModelInputContent( TimeSeriesArray array ) {
		return IntStream.range( 0, array.size() )
			.mapToObj( i -> Strman.append( TimeLightUtils.toString( new DateTime( array.getTime( i ) ), TIME_FORMAT, TimeLightUtils.UTC8 ),
					",", TimeSeriesLightUtils.getValue( array, i ).toString() ) )
			.collect( Collectors.joining( System.getProperty( "line.separator" ) ) );
	}
}
