package tw.fondus.fews.adapter.pi.runoff.nchc;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );
		
		Path executablePath = Prevalidated.checkExists( 
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC RainRunoff ExecutableAdapter: The model executable directory not exist." );
		
		Path parameterPath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getParametersPath() ),
				"NCHC RainRunoff ExecutableAdapter: The model parameters directory not exist." );

		Path executableInput =  executablePath.resolve( modelArguments.getInputs().get( 0 ) );
		String parameterInput = executablePath.resolve( modelArguments.getInputs().get( 1 ) ).toString();
		Path modelOutput = executablePath.resolve( modelArguments.getOutputs().get( 0 ) );
		String parameterPrefix = Strman.append( FileUtils.getNameWithoutExt( parameterInput ), Strings.UNDERLINE );

		logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Prepare to running model." );

		// Get model parameters use recursive
		this.readParameters( parameterPath, parameterPrefix );
		
		// Create executable command
		String executable = modelArguments.getExecutable();
		String command = executablePath.resolve( executable ).toString();
		
		// Read the model inputs inside the Input/ folder.
		PathUtils.list( inputPath, path -> PathUtils.equalsExtension( path, FileType.TXT ) )
			.forEach( path -> {
				String locationId = PathUtils.getNameWithoutExtension( path );
				if ( this.parametersMap.containsKey( locationId ) ){
					// Move model input to executable directory
					logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Move model input to executable directory. Processing {} now.", locationId );
					PathUtils.move( path, executableInput );
					logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Copy input parameter file to executable directory." );
					PathUtils.copy( this.parametersMap.get( locationId ), parameterInput );

					// Run model
					logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Running executable." );
					try {
						Executions.execute( executor -> executor.directory( executablePath.toFile() ), command );
					} catch (IOException | InterruptedException | TimeoutException e) {
						logger.log( LogLevel.ERROR, "NCHC RainRunoff ExecutableAdapter: Running model process has something wrong." );
					}

					// Move model output to output directory
					logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Move model output to output directory. Processing {} now.", locationId );
					PathUtils.move( modelOutput, outputPath.resolve( PathUtils.getName( path ) ) );

				} else {
					logger.log( LogLevel.WARN, "NCHC RainRunoff ExecutableAdapter: Parameter file directory not contain {}.", locationId );
				}
			} );

		logger.log( LogLevel.INFO, "NCHC RainRunoff ExecutableAdapter: Finished to running adapter." );
	}
	
	/**
	 * Read all parameter files to parametersMap.
	 *
	 * @param basePath base path
	 * @param prefix prefix
	 */
	private void readParameters( Path basePath, String prefix ){
		PathUtils.list( basePath ).forEach( path -> {
			if ( Files.isDirectory( path ) ) {
				this.readParameters( path, prefix );
			} else {
				this.addParameterFiles( path, prefix );
			}
		} );
	}
	
	/**
	 * The logic of parse model parameter files to parametersMap.
	 * 
	 * @param parameterFile parameter file
	 * @param prefix prefix
	 */
	protected abstract void addParameterFiles( Path parameterFile, String prefix );
}
