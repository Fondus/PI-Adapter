package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.ExecutableArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model post-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHPostAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ExecutableArguments arguments = new ExecutableArguments();
		new KWGIUHPostAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		ExecutableArguments executableArguments = (ExecutableArguments) arguments;

		Path executableDir = Prevalidated.checkExists(
				Strman.append( basePath.toString(), PATH, executableArguments.getExecutableDir() ),
				"KWGIUHPostAdapter: Can not find executable directory." );

		Path rainfallPath = Prevalidated.checkExists( inputPath.resolve( executableArguments.getInputs().get( 0 ) ),
				"KWGIUHPostAdapter: Can not find the file of rainfall input." );

		Path modelOutputPath = Prevalidated.checkExists(
				executableDir.resolve( executableArguments.getInputs().get( 1 ) ),
				"KWGIUHPostAdapter: Can not find the file of model outout." );

		try {
			logger.log( LogLevel.INFO, "KWGIUHPostAdapter: Starting KWGIUHPostAdapter process." );
			TimeSeriesArray timeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( rainfallPath ).get( 0 );

			logger.log( LogLevel.INFO, "KWGIUHPostAdapter: Filling timeseries header for model output." );
			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			TimeSeriesLightUtils.fillPiTimeSeriesHeader( handler, executableArguments.getInputs().get( 2 ),
					executableArguments.getParameter(), executableArguments.getUnit() );
			DateTime startTime = new DateTime( timeSeriesArray.getStartTime() );

			logger.log( LogLevel.INFO, "KWGIUHPostAdapter: Reading model output and convert to Pi-XML format." );
			List<String> lines = Files.readAllLines( modelOutputPath, Charset.forName( "Cp1252" ) );
			int outputDataSize = timeSeriesArray.size() + 144;
			for ( int data = 0; data <= outputDataSize; data++ ) {
				String line = lines.get( data + 27 );
				float value = Float.valueOf( line.substring( 31, line.length() ).trim() );
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, startTime.plusHours( data ).getMillis(), value );
			}

			logger.log( LogLevel.INFO, "KWGIUHPostAdapter: Writing Pi-XML format file of model output." );
			TimeSeriesLightUtils.writePIFile( handler,
					outputPath.resolve( executableArguments.getOutputs().get( 0 ) ).toString() );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR,
					"KWGIUHPostAdapter: Reading file of model output, rainfall or writing file has something wrong." );
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "KWGIUHPostAdapter: Reading file of rainfall has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "KWGIUHPostAdapter: Writing output file has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "KWGIUHPostAdapter: End KWGIUHPostAdapter process." );
	}
}
