package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.zeroturnaround.exec.InvalidExitValueException;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Parent class with Model executable-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffExecutable extends PiCommandLineExecute {
	protected final Map<String, String> parametersMap = new HashMap<>();
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path executablePath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getExecutablePath()) );
		Prevalidated.checkExists( executablePath, "NCHC RainRunoff ExecutableAdapter: The model executable directory not exist." );
		
		Path parameterPath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getParametersPath()) );
		Prevalidated.checkExists( parameterPath, "NCHC RainRunoff ExecutableAdapter: The model parameters directory not exist." );
		
		String executableDirPath = Strman.append( executablePath.toString(), StringUtils.PATH);
		String executableInput =  Strman.append( executableDirPath, modelArguments.getInputs().get(0) );
		String parameterInput = Strman.append( executableDirPath, modelArguments.getInputs().get(1) );
		String modelOutput = Strman.append( executableDirPath, modelArguments.getOutputs().get(0) );
		String parameterPrefix = Strman.append( FileUtils.getNameWithoutExt( parameterInput ), StringUtils.UNDERLINE );
		
		/** Get model parameters use recursive **/
		this.readParameters( logger, parameterPath, parameterPrefix );
		
		/** Create executable command **/
		String executable = modelArguments.getExecutable();
		String command = Strman.append( executablePath.toString(), PATH, executable );
		
		// Read the model inputs inside the Input/ folder.
		try ( Stream<Path> paths = Files.list( inputPath ) ) {
			paths.filter( path -> PathUtils.getFileExtension( path ).equals( FileType.TXT.getType() ) )
				.forEach( path -> {
					String locationId = PathUtils.getNameWithoutExtension( path );
					
					if ( this.parametersMap.containsKey( locationId ) ){
						try {
							/** Move model input to executable directory **/
							logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Move model input to executable directory. Processing {} now.", locationId );
							FileUtils.move( path.toString(), executableInput );
							
							logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Copy input parameter file to executable directory." );
							FileUtils.copy( this.parametersMap.get( locationId ), parameterInput );
							
							/** Run model **/
							logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Running executable." );
							Executions.execute( executor -> executor.directory( executablePath.toFile() ),
									command );
							
							/** Move model output to output directory **/
							logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Move model output to output directory. Processing {} now.", locationId );
							FileUtils.move( modelOutput, 
									Strman.append( outputPath.toString(), PATH, PathUtils.getName( path ) ));
							
						} catch (IOException | InvalidExitValueException | InterruptedException | TimeoutException e) {
							logger.log( LogLevel.ERROR, "NCHC RainRunoff ExecutableAdapter: Running model process has something wrong." );
						} 
					} else {
						logger.log( LogLevel.WARN, "NCHC RainRunoff ExecutableAdapter: Parameter file directory not contain {}.", locationId );
					}
				} );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RainRunoff ExecutableAdapter: The walk input directory has something wrong." );
		}
	}
	
	/**
	 * Read all parameter files to parametersMap.
	 * 
	 * @param logger
	 * @param basePath
	 * @param prefix
	 * @throws FileNotFoundException
	 */
	private void readParameters( PiDiagnosticsLogger logger, Path basePath, String prefix ){
		if ( Files.exists( basePath ) && Files.isDirectory( basePath ) ) {
			try ( Stream<Path> paths = Files.list( basePath ) ) {
				paths.forEach( path -> {
					if ( Files.isDirectory( path ) ) {
						this.readParameters( logger, path, prefix );
					} else {
						this.addParameterFiles( path, prefix );
					}
				} );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "NCHC RainRunoff ExecutableAdapter: Read the parameters process has something wrong." );
			}
		}
	}
	
	/**
	 * The logic of parse model parameter files to parametersMap.
	 * 
	 * @param parameterFile
	 * @param prefix
	 */
	protected abstract void addParameterFiles( Path parameterFile, String prefix );
}
