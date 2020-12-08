package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.PostAdapterArguments;

/**
 * Model post-adapter for running Virtual IoT model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class VirtualIoTPostAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PostAdapterArguments arguments = PostAdapterArguments.instance();
		new VirtualIoTPostAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PostAdapterArguments adapterArguments = (PostAdapterArguments) arguments;

		try {
			logger.log( LogLevel.INFO, "VirtualIotPostAdapter: Starting post-Adapter process." );
			PathUtils.clean( outputPath );
			
			Path depthPath = Prevalidated.checkExists( inputPath.resolve( adapterArguments.getInputs().get( 0 ) ),
					"VirtualIotPostAdapter: Can not find executable directory." );
			TimeSeriesArray timeSeriesArray = TimeSeriesLightUtils.read( depthPath ).get( 0 );

			Path executablePath = Prevalidated.checkExists( basePath.resolve( adapterArguments.getExecutableDir() ),
					"VirtualIotPostAdapter: Can not find executable directory." );

			Path modelOutputPath = Prevalidated.checkExists(
					executablePath.resolve( adapterArguments.getInputs().get( 1 ) ),
					"VirtualIotPostAdapter: Can not find model output file in executable directory." );
			List<String> modelOutputs = PathReader.readAllLines( modelOutputPath );

			Path orderPath = Prevalidated.checkExists( executablePath.resolve( adapterArguments.getInputs().get( 2 ) ),
					"VirtualIotPostAdapter: Can not find output order file in executable directory." );
			List<String> outputOrder = PathReader.readAllLines( orderPath );

			SimpleTimeSeriesContentHandler handler = TimeSeriesLightUtils.seriesHandler();
			IntStream.range( 0, outputOrder.size() ).forEach( i -> {
				TimeSeriesLightUtils.addHeader( handler, outputOrder.get( i ), adapterArguments.getParameter(),
						adapterArguments.getUnit() );
				
				TimeSeriesLightUtils.addValue( handler, timeSeriesArray.getEndTime(),
						NumberUtils.create( modelOutputs.get( i ).split( Strings.SPLIT_SPACE_MULTIPLE )[3] ) );
			} );
			
			logger.log( LogLevel.INFO, "VirtualIotPostAdapter: Writing Pi-XML format file of model output." );
			TimeSeriesLightUtils.write( handler, outputPath.resolve( adapterArguments.getOutputs().get( 0 ) ) );
			
			logger.log( LogLevel.INFO, "VirtualIotPostAdapter: End post-Adapter process." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "VirtualIotPostAdapter: Reading XML file of depth has something wrong." );
		}
	}
	
}
