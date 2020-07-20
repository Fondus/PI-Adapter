package tw.fondus.fews.adapter.pi.irrigation.nchc;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.PreArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.math.BigDecimal;
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
@SuppressWarnings( "rawtypes" )
public class IrrigationOptimizePreAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PreArguments arguments = PreArguments.instance();
		new IrrigationOptimizePreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PreArguments modelArguments = this.asArguments( arguments, PreArguments.class ) ;

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0  ) ),
				"NCHC Irrigation-Optimize PreAdapter: The XML file is not exist." );

		Path executablePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC Irrigation-Optimize PreAdapter: The executable folder is not exist." );

		try {
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );

			String modelInputFileName = modelArguments.getOutputs().get( 0 );
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PreAdapter: Start create model input file: {}.", modelInputFileName );

			String modelInputContent = this.createModelInput( timeSeriesArrays );
			Path modelInputPath = inputPath.resolve( modelInputFileName );

			PathWriter.write( modelInputPath, modelInputContent );
			PathUtils.copy( modelInputPath, executablePath );

			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize PreAdapter: Finished create model input file: {}.", modelInputFileName );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize PreAdapter: No time series found in file in the PI-XML!" );
		}
	}

	/**
	 * Create the model input content.
	 *
	 * @param timeSeriesArrays time series arrays
	 * @return model content
	 */
	private String createModelInput( TimeSeriesArrays timeSeriesArrays ){
		TimeSeriesArray array = timeSeriesArrays.get( 0 );

		return IntStream.range( 0, array.size() )
				.mapToObj( i -> TimeSeriesLightUtils.getValue( array, i, Numbers.ONE ) )
				.map( BigDecimal::toString )
				.collect( Collectors.joining( Strings.SPLIT_TAB ) );
	}
}
