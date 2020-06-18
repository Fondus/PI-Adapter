package tw.fondus.fews.adapter.pi.test;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.test.argument.IndexArguments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The stress test adapter it's used to forward time series from input PI-XML by specified index.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ForwardTimeContentByIndexAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		IndexArguments arguments = IndexArguments.instance();
		new ForwardTimeContentByIndexAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Start the adapter process." );
		// Cast PiArguments to expand arguments
		IndexArguments modelArguments = this.asArguments( arguments, IndexArguments.class );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"ForwardTimeContentByIndexAdapter: The input XML not exists!" );

		int start = modelArguments.getStart();
		int end = modelArguments.getEnd();
		int length = modelArguments.getLength();

		logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: The arguments time content length is {}.", length );
		if ( end >= length ){
			logger.log( LogLevel.WARN, "ForwardTimeContentByIndexAdapter: The end {} exceed or equals to the time content length {}.", end, length );
		} else if ( start >= 0 ){
			logger.log( LogLevel.WARN, "ForwardTimeContentByIndexAdapter: The start {} exceed or equals to the 0.", start);
		} else {
			// Meta info part
			Path metaPath = basePath.resolve( modelArguments.getInputs().get( 1 ) );
			logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Try to read the meta info file with path: {}.", metaPath.toString() );
			if ( PathUtils.isNotExists( metaPath ) ){
				logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Meta info file with path: {} not exists, will create with default value.", metaPath.toString() );
				this.resetMetaInfo( metaPath );
			}

			// Index part
			List<String> lines = PathReader.readAllLines( metaPath );
			int currentIndex = Integer.parseInt( CollectionUtils.last( lines ) );
			int timeZeroIndex = currentIndex;
			logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: The current index is {}, it's will be use to create forward to proxy.", currentIndex );

			if ( currentIndex >= length ){
				logger.log( LogLevel.WARN, "ForwardTimeContentByIndexAdapter: The current index {} exceed the time content length {}, will reset to default value.", currentIndex, length );
				this.resetMetaInfo( metaPath );
				currentIndex = 0;
				timeZeroIndex = 0;
			}

			int startIndex = timeZeroIndex + start;
			int endIndex = timeZeroIndex + end;
			if ( timeZeroIndex == 0 || startIndex < 0 ){
				startIndex = 0;
				timeZeroIndex = start * -1;
				endIndex = timeZeroIndex + end;
			}

			if ( endIndex >= length ){
				endIndex = length - 1;
				timeZeroIndex = endIndex - end;
				startIndex = timeZeroIndex + start;
			}

			logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: The historical start index is {}, and time zero index is {}.", startIndex, timeZeroIndex );
			logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: The forecast time zero index is {}, and end index is {}.", timeZeroIndex, endIndex );

			// Time Series part
			try {
				logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Try to read the PI-XML." );
				TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.read( inputXML );

				int size = timeSeriesArrays.get( 0 ).size();
				if ( size != length ){
					logger.log( LogLevel.WARN, "ForwardTimeContentByIndexAdapter: The arguments time content length {} and time series size {} not same, adapter will do nothing.", length, size );
				} else {
					logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Try to write the PI-XML will be forward to proxy." );
					try {
						// Write historical
						this.writePiXML( outputPath.resolve( modelArguments.getOutputs().get( 0 ) ), timeSeriesArrays, startIndex, timeZeroIndex );

						// Write forecast
						this.writePiXML( outputPath.resolve( modelArguments.getOutputs().get( 1 ) ), timeSeriesArrays, timeZeroIndex, endIndex );

						// Write Meta info by new index
						currentIndex = currentIndex + 1;
						PathWriter.append( metaPath, Strings.BREAKLINE + currentIndex );

					} catch (IOException e ){
						logger.log( LogLevel.ERROR, "ForwardTimeContentByIndexAdapter: Write the PI-XML has something wrong!" );
					}
				}
			} catch (IOException e ){
				logger.log( LogLevel.ERROR, "ForwardTimeContentByIndexAdapter: No time series found in file in the PI-XML!" );
			}
		}
		logger.log( LogLevel.INFO, "ForwardTimeContentByIndexAdapter: Finished the adapter process." );
	}

	/**
	 * Reset the meta info value.
	 *
	 * @param path meta info path
	 */
	private void resetMetaInfo( Path path ){
		PathWriter.write( path, "0" );
	}

	/**
	 * Extract the values by index and write the PI XML.
	 *
	 * @param path output path of pi xml
	 * @param timeSeriesArrays time series array
	 * @param start start index
	 * @param end end index
	 * @throws IOException has IO Exception
	 */
	private void writePiXML( Path path, TimeSeriesArrays timeSeriesArrays, int start, int end ) throws IOException {
		SimpleTimeSeriesContentHandler handler = TimeSeriesUtils.seriesHandler();
		TimeSeriesUtils.toList( timeSeriesArrays ).forEach( array -> {
			TimeSeriesUtils.addHeader( handler, array.getHeader() );

			IntStream.rangeClosed( start, end ).forEach( i ->
				TimeSeriesUtils.addValue( handler, array.getTime( i ), TimeSeriesUtils.getValue( array, i ) )
			);
		} );
		TimeSeriesUtils.write( handler, path );
	}
}
