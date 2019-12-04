package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.ExecutableArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * Model executable-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ExecutableArguments arguments = new ExecutableArguments();
		new KWGIUHExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		ExecutableArguments executableArguments = (ExecutableArguments) arguments;

		try {
			logger.log( LogLevel.INFO, "KWGIUHExecutable: Starting KWGIUHExecutable process." );
			Path executableDir = Prevalidated.checkExists(
					Strman.append( basePath.toString(), PATH, executableArguments.getExecutableDir() ),
					"KWGIUHExecutable: Can not find executable directory." );

			logger.log( LogLevel.INFO, "KWGIUHExecutable: Coping model input file from input directory." );
			PathUtils.copy( inputPath.resolve( executableArguments.getInputs().get( 0 ) ), executableDir );
			PathUtils.copy( inputPath.resolve( "temp.txt" ), executableDir );

			logger.log( LogLevel.INFO, "KWGIUHExecutable: Running mdoel." );
			String command = executableDir.resolve( executableArguments.getExecutable().get( 0 ) ).toString();
			Executions.execute( executor -> executor.directory( executableDir.toFile() ), command );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "KWGIUHExecutable: Coping input file has something wrong." );
		} catch (InvalidExitValueException e) {
			logger.log( LogLevel.ERROR, "KWGIUHExecutable: Running model has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "KWGIUHExecutable: Running model has something wrong." );
		} catch (TimeoutException e) {
			logger.log( LogLevel.ERROR, "KWGIUHExecutable: Running model has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "KWGIUHExecutable: End KWGIUHExecutable process." );
	}

}
