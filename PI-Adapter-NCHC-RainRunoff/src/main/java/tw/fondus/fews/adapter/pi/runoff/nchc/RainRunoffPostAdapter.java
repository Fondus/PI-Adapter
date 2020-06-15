package tw.fondus.fews.adapter.pi.runoff.nchc;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parent class with Model post-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffPostAdapter extends PiCommandLineExecute {
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = this.asIOArguments( arguments );

		Path timeMetaInfoPath = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get(0) ),
				"NCHC RainRunoffPostAdapter: The time meta-information not exists!" );

		// Read time meta-information
		String timeMetaContent = PathReader.readString( timeMetaInfoPath );
		if ( Strman.isBlank( timeMetaContent ) ){
			logger.log( LogLevel.WARN, "NCHC RainRunoffPostAdapter: The time meta-information not contain any content." );
		} else {
			String[] temps = timeMetaContent.split( Strings.SPLIT_SPACE_MULTIPLE );
			long startTimeMillis = Long.parseLong( temps[0] );
			long timeStepMillis = Long.parseLong( temps[1] );

			logger.log( LogLevel.INFO, "NCHC RainRunoff PostAdapter: Start read model output files to PiXML." );
			// Read model output and create XML content
			SimpleTimeSeriesContentHandler contentHandler = TimeSeriesLightUtils.seriesHandler();
			PathUtils.list( outputPath, path -> PathUtils.equalsExtension( path, FileType.TXT ) )
				.forEach( path -> {
					// Parse the model output
					logger.log( LogLevel.INFO, "NCHC RainRunoff PostAdapter: Parse the model output file: {} into handler.", PathUtils.getNameWithoutExtension( path ) );
					this.parseModelOutputContent( path, contentHandler,
							modelArguments.getParameter(), modelArguments.getUnit(),
							startTimeMillis, timeStepMillis );
				} );

			try {
				// Write PI-XML
				TimeSeriesLightUtils.write( contentHandler, outputPath.resolve( modelArguments.getOutputs().get( 0 ) ) );
			} catch ( IOException e ){
				logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: Write model output to PI-XML has something wrong." );
			}
		}
		logger.log( LogLevel.INFO, "NCHC RainRunoff PostAdapter: Finished read model output files to PiXML." );
	}
	
	/**
	 * Read model output content -> SimpleTimeSeriesContentHandler logic.
	 * 
	 * @param outputPath		: model output.
	 * @param contentHandler	: fews pi xml pojo for write.
	 * @param parameter			: model parameter.
	 * @param unit				: model parameter unit.
	 * @param startTimeMillis	: start time with millisecond.
	 * @param timeStepMillis	: time step with millisecond.
	 */
	protected abstract void parseModelOutputContent( Path outputPath,
			SimpleTimeSeriesContentHandler contentHandler, String parameter, String unit,
			long startTimeMillis, long timeStepMillis);
}
