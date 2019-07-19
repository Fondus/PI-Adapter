package tw.fondus.fews.adapter.pi.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The Model pre-adapter for running example model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePreAdapter extends PiCommandLineExecute {
	private static final String TIME_FORMAT = "yyyyMMddHHmm";
	private static final String TXT = ".txt";
	
	public static void main(String[] args) {
		PiIOArguments arguments = new PiIOArguments();
		new ExamplePreAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = (PiIOArguments) arguments;
		
		try {
			Path inputXML = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0)),
					"Example PreAdapter: The input XML do not exists!" );
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			
			logger.log( LogLevel.INFO, "Example PreAdapter: Start create model input files.");
			
			TimeSeriesArray array = timeSeriesArrays.get( 0 );
			String modelInput = this.createModelInputContent( array );
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, array.getHeader().getLocationId(), TXT ), modelInput );
			
			logger.log( LogLevel.INFO, "Example PreAdapter: Finished create model input files.");
			
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "Example PreAdapter: Read XML not exists or content empty!");
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "Example PreAdapter: The adapter has something wrong!" );
		}
	}
	
	/**
	 * Mapping timeSeriesArray -> model input content logic.
	 * 
	 * @param array
	 * @return
	 */
	private String createModelInputContent( TimeSeriesArray array ) {
		return IntStream.range( 0, array.size() )
			.mapToObj( i -> Strman.append( TimeLightUtils.toString( new DateTime( array.getTime( i ) ), TIME_FORMAT, TimeLightUtils.UTC8 ),
					",", String.valueOf( TimeSeriesLightUtils.getValue( array, i ) ) ) )
			.collect( Collectors.joining( System.getProperty( "line.separator" ) ) );
	}
}
