package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.zeroturnaround.exec.InvalidExitValueException;

import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.flow.longtime.nchc.argument.RunArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * The model executable for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		RunArguments arguments = RunArguments.instance();
		new LTFExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;

		Path executablePath = Prevalidated.checkExists( basePath.resolve( modelArguments.getExecutableDir() ),
				"NCHC LTF Executable: The model executable directory not exist." );

		Path templatePath = Prevalidated.checkExists( basePath.resolve( modelArguments.getTemplateDir() ),
				"NCHC LTF Executable: The template directory is not exist." );

		logger.log( LogLevel.INFO, "NCHC LTF Executable: Start the executable process." );

		try {
			PathUtils.clean( executablePath );

			/** Copy model input file to executable directory **/
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 0 ) ), executablePath );
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 1 ) ), executablePath );
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 2 ) ), executablePath );

			/** Copy model to executable directory from template directory **/
			this.copies( templatePath, executablePath, false );
			this.copies( templatePath.resolve( "Basin" ).resolve( modelArguments.getProjectName() ), executablePath, true );

			/** Replacing arguments of forecast and observed steps. **/
			List<String> lines = Files.readAllLines( executablePath.resolve( modelArguments.getInputs().get( 3 ) ) );
			IntStream.range( 0, lines.size() ).forEach( line -> {
				if ( lines.get( line ).contains( "{forecast_steps}" ) ) {
					lines.set( line, lines.get( line )
							.replace( "{forecast_steps}", modelArguments.getForecastSteps().toString() ) );
				} else if ( lines.get( line ).contains( "{observed_steps}" ) ) {
					lines.set( line, lines.get( line )
							.replace( "{observed_steps}", modelArguments.getObservedSteps().toString() ) );
				}
			} );
			PathWriter.write( executablePath.resolve( modelArguments.getInputs().get( 3 ) ),
					lines.stream().collect( Collectors.joining( Strings.BREAKLINE ) ), Strings.UTF8_CHARSET );

			String command = executablePath.resolve( modelArguments.getExecutable().get( 0 ) ).toString();

			/** Run model **/
			Executions.execute( executor -> executor.directory( executablePath.toFile() ), command );
			PathUtils.copy( executablePath.resolve( modelArguments.getOutputs().get( 0 ) ), outputPath );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF Executable: has IO problem." );
		} catch (InvalidExitValueException | InterruptedException | TimeoutException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF Executable: executable has something problem." );
		}

		logger.log( LogLevel.INFO, "NCHC LTF Executable: Finished the executable process." );
	}

	/**
	 * Copy directory with sub directory or not.
	 * 
	 * @param source
	 * @param dest
	 * @param withDirectory
	 */
	private void copies( Path source, Path dest, boolean withDirectory ) {
		PathUtils.list( source ).forEach( path -> {
			if ( PathUtils.isDirectory( path ) ) {
				if ( withDirectory ) {
					copies( path, dest, true );
				}
			} else {
				PathUtils.copy( path, dest );
			}
		} );
	}
}
