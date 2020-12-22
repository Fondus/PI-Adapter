package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.PreAdapterArguments;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Model pre-adapter for running Virtual IoT model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class VirtualIoTPreAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PreAdapterArguments arguments = PreAdapterArguments.instance();
		new VirtualIoTPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PreAdapterArguments adapterArguments = this.asArguments( arguments, PreAdapterArguments.class );
		try {
			logger.log( LogLevel.INFO, "VirtualIotPreAdapter: Starting Pre-Adapter process." );
			Path depthPath = Prevalidated.checkExists( inputPath.resolve( adapterArguments.getInputs().get( 0 ) ),
					"VirtualIotPreAdapter: Can not input XML file of depth." );
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( depthPath );
			Map<String, TimeSeriesArray> timeSeriesMap = TimeSeriesUtils.toMap( timeSeriesArrays );

			Path orderPath = Prevalidated.checkExists(
					basePath.resolve( adapterArguments.getTemplateDir() )
							.resolve( "Basin" )
							.resolve( adapterArguments.getBasin() )
							.resolve( adapterArguments.getInputs().get( 1 ) ),
					"VirtualIotPreAdapter: Can not find input order file in basin template directory." );
			List<String> inputOrder = PathReader.readAllLines( orderPath );

			logger.log( LogLevel.INFO, "VirtualIotPreAdapter: Writing model input file." );
			List<String> inputContents = this.buildModelInput( inputOrder, timeSeriesMap );
			if ( inputContents.size() == inputOrder.size() ) {
				PathWriter.write( inputPath.resolve( adapterArguments.getOutputs().get( 0 ) ),
						String.join( Strings.BREAKLINE, inputContents ) );
			} else {
				logger.log( LogLevel.ERROR, "VirtualIotPreAdapter: Missing some input data by order ids." );
			}
			
			logger.log( LogLevel.INFO, "VirtualIotPreAdapter: Finished Pre-Adapter process." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "VirtualIotPreAdapter: Reading XML file of depth has something wrong." );
		}
	}

	/**
	 * Building model input content
	 * 
	 * @param inputOrder input id order
	 * @param timeSeriesMap map of timeseries data
	 * @return model input content
	 */
	private List<String> buildModelInput( List<String> inputOrder, Map<String, TimeSeriesArray> timeSeriesMap ) {
		return inputOrder.stream()
				.filter( timeSeriesMap::containsKey )
				.map( id -> {
					TimeSeriesArray timeSeriesArray = timeSeriesMap.get( id );
					return IntStream.range( 0, timeSeriesArray.size() )
							.mapToObj( i -> TimeSeriesLightUtils.getValue( timeSeriesArray, i, BigDecimal.ZERO ).toString() )
							.collect( Collectors.joining( Strings.COMMA ) );
				} )
				.collect( Collectors.toList() );
	}
}
