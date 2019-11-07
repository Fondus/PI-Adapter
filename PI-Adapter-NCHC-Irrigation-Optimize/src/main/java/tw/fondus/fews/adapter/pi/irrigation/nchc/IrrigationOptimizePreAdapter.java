package tw.fondus.fews.adapter.pi.irrigation.nchc;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.PreArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model pre-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use to create model main input.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizePreAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PreArguments arguments = new PreArguments();
		new IrrigationOptimizePreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PreArguments modelArguments = (PreArguments) arguments;

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0  ) ),
				"NCHC Irrigation-Optimize PreAdapter: The XML file is not exist." );

		Path executablePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC Irrigation-Optimize PreAdapter: The executable folder is not exist." );

		try {
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );

			String modelInputFileName = modelArguments.getOutputs().get( 0 );
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PreAdapter: Start create model input file: {}.", modelInputFileName );

			String modelInputContent = this.createModelInput( timeSeriesArrays );
			Path modelInputPath = inputPath.resolve( modelInputFileName );

			FileUtils.writeText( modelInputPath.toString(), modelInputContent );
			PathUtils.copy( modelInputPath, executablePath );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PreAdapte: Finished create model input file: {}.", modelInputFileName );

		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize PreAdapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize PreAdapter: Read XML has something failed!" );
		}
	}

	/**
	 * Create the model input content.
	 *
	 * @param timeSeriesArrays
	 * @return
	 */
	private String createModelInput( TimeSeriesArrays timeSeriesArrays ){
		TimeSeriesArray array = timeSeriesArrays.get( 0 );

		return IntStream.range( 0, array.size() )
				.mapToObj( i -> TimeSeriesLightUtils.getValue( array, i, 0f ) )
				.map( value -> String.valueOf( value ) )
				.collect( Collectors.joining( StringUtils.TAB ) );
	}
}
