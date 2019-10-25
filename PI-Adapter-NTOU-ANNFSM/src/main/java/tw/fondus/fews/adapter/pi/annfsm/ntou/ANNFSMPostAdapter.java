package tw.fondus.fews.adapter.pi.annfsm.ntou;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.ExecutableArguments;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model post-adapter for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class ANNFSMPostAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ExecutableArguments arguments = new ExecutableArguments();
		new ANNFSMPostAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ANNFSMPostAdapter: Starting ANNFSMPostAdapter process." );
		ExecutableArguments executableArguments = (ExecutableArguments) arguments;

		Path executablePath = Prevalidated.checkExists( basePath.resolve( executableArguments.getExecutableDir() ),
				"ANNFSMPostAdapter: The directory of executable is not exist." );
		Path modelOutputPath = Prevalidated.checkExists(
				executablePath.resolve( executableArguments.getInputs().get( 0 ) ),
				"ANNFSMPostAdapter: The file of model output is not exist." );
		Path tidePath = Prevalidated.checkExists( inputPath.resolve( executableArguments.getInputs().get( 1 ) ),
				"ANNFSMPostAdapter: The XML file of tide is not exist." );

		try {
			logger.log( LogLevel.INFO, "ANNFSMPostAdapter: Reading model output and convert to Pi-XML format." );
			TimeSeriesArray tideSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( tidePath ).get( 0 );

			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			TimeSeriesLightUtils.fillPiTimeSeriesHeader( handler, tideSeriesArray.getHeader().getLocationId(),
					"H.tide.simulated", "cm" );

			DateTime inputEndTime = new DateTime( tideSeriesArray.getEndTime() );
			List<String> lines = Files.readAllLines( modelOutputPath );
			IntStream.range( 0, lines.size() ).forEach( data -> {
				float value = Float.valueOf( lines.get( data ).trim() );
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, inputEndTime.plusHours( data + 1 ).getMillis(),
						value );
			} );

			logger.log( LogLevel.INFO, "ANNFSMPostAdapter: Writing Pi-XML format file of model output." );
			TimeSeriesLightUtils.writePIFile( handler,
					outputPath.resolve( executableArguments.getOutputs().get( 0 ) ).toString() );
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "ANNFSMPostAdapter: Reading pi timeseries data of tide has something wrong." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR,
					"ANNFSMPostAdapter: Reading file of model output, tide data or writing file has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "ANNFSMPostAdapter: Writing output file has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "ANNFSMPostAdapter: End ANNFSMPostAdapter process." );
	}
}
