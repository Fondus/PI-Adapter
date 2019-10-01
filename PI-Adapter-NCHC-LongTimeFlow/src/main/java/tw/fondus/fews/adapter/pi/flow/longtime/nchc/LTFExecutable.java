package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
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
		RunArguments arguments = new RunArguments();
		new LTFExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;

		Path executablePath = Prevalidated.checkExists(
				Strman.append( basePath.toString(), PATH, modelArguments.getExecutableDir() ),
				"NCHC LTF Executable: The model executable directory not exist." );

		Path templatePath = Prevalidated.checkExists(
				Strman.append( basePath.toString(), PATH, modelArguments.getTemplateDir() ),
				"NCHC LTF Executable: The template directory is not exist." );

		logger.log( LogLevel.INFO, "NCHC LTF Executable: Start the executable process." );

		try {
			FileUtils.cleanDirectory( executablePath.toFile() );

			/** Copy model input file to executable directory **/
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 0 ) ), executablePath );
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 1 ) ), executablePath );
			PathUtils.copy( inputPath.resolve( modelArguments.getInputs().get( 2 ) ), executablePath );

			/** Copy model to executable directory from template directory **/
			PathUtils.copyDirectory( templatePath, executablePath, true );
			PathUtils.copyDirectory( templatePath.resolve( "Basin" ).resolve( modelArguments.getProjectName() ),
					executablePath, false );

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
			FileUtils.writeStringToFile( executablePath.resolve( modelArguments.getInputs().get( 3 ) ).toFile(),
					lines.stream().collect( Collectors.joining( StringUtils.BREAKLINE ) ) );

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
}
