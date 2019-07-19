package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

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
		PiIOArguments modelArguments = (PiIOArguments) arguments;
		
		try {
			Path timeMetaInfoPath = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0) ),
					"NCHC RainRunoffPostAdapter: The time meta-information not exists!" );
			
			// Read time meta-information
			Optional<String> optTimeInfos = this.readTimeMeta( timeMetaInfoPath );
			OptionalUtils.ifPresentOrElse( optTimeInfos,
					timeInfos -> {
						String[] temps = timeInfos.split( StringUtils.SPACE_MULTIPLE );
						long startTimeMillis = Long.valueOf( temps[0] );
						long timeStepMillis = Long.valueOf( temps[1] );
						
						logger.log( LogLevel.INFO, "NCHC RainRunoff PostAdapter: Start read model output files to PiXML." );
						
						/** Read model output and create XML content **/
						SimpleTimeSeriesContentHandler contentHandler = new SimpleTimeSeriesContentHandler();
						try ( Stream<Path> paths = Files.list( outputPath ) ) {
							
							paths.filter( path -> PathUtils.getFileExtension( path ).equals( FileType.TXT.getType() ) )
								.forEach( path -> {
									try {
										// Parse the model output
										this.parseModelOutputContent( path, contentHandler,
												modelArguments.getParameter(), modelArguments.getUnit(),
												startTimeMillis, timeStepMillis );
									} catch (IOException e) {
										logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: Read model output file has something wrong." );
									}
								} );
							
							// Write PI-XML
							TimeSeriesLightUtils.writePIFile( contentHandler, Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get(0) ));
							
						} catch (IOException e) {
							logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: Read model output files has something wrong." );
						} catch (InterruptedException e) {
							logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: Write model output files to PI-XML has something wrong." );
						}
						
						logger.log( LogLevel.INFO, "NCHC RainRunoff PostAdapter: Finished read model output files to PiXML." );
					},
					() -> logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: The time meta-information has no content!" ) );
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RainRunoff PostAdapter: Read time meta-information has something wrong!" );
		} finally {
			
		}
	}
	
	/**
	 * Read temporary time meta-information.
	 * 
	 * @param timeMetaInfoPath
	 * @return
	 * @throws IOException
	 */
	private Optional<String> readTimeMeta( Path timeMetaInfoPath ) throws IOException {
		List<String> fileLines = PathUtils.readAllLines( timeMetaInfoPath );
		return Optional.ofNullable(fileLines.get(0));
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
	 * @throws IOException		: 
	 */
	protected abstract void parseModelOutputContent( Path outputPath,
			SimpleTimeSeriesContentHandler contentHandler, String parameter, String unit,
			long startTimeMillis, long timeStepMillis) throws IOException;
}
