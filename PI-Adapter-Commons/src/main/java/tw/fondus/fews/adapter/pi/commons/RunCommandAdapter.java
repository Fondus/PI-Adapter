package tw.fondus.fews.adapter.pi.commons;

import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.JCommanderRunner;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiCommandArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.logger.PiDiagnosticsUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * The commons adapter tool it used to running command with FEWS.
 *
 * @author Brad Chen
 *
 */
@Slf4j
public class RunCommandAdapter {
	public static void main( String[] args ){
		PiCommandArguments arguments = PiCommandArguments.instance();
		new RunCommandAdapter().execute( args, arguments );
	}

	/**
	 * Use arguments to execute program with command-line interface. Will check basic arguments.
	 *
	 * @param args the command-line arguments
	 * @param arguments the program arguments
	 */
	public void execute( String[] args, PiCommandArguments arguments ) {
		JCommanderRunner.execute( args, arguments, this.getClass().getSimpleName(), commandArguments -> {
			Path basePath = commandArguments.getBasePath();

			// Initialize Logger
			PiDiagnosticsLogger logger = PiDiagnosticsUtils.initializeLogger( basePath, commandArguments.getLogPath(),
					commandArguments.getDiagnostics() );

			PiDiagnosticsUtils.adapterProcessInLoggerScope( logger, () -> {
				String command = commandArguments.getCommand();
				logger.log( LogLevel.INFO, "RunCommandAdapter: Try to run the command {}.", command );

				try {
					Executions.execute( executor -> executor
									.directory( basePath.toFile() )
									.redirectOutput( Slf4jStream.of( getClass() ).asInfo() ),
							command.split( " " ) );
				} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
					logger.log( LogLevel.ERROR, "RunCommandAdapter: Running command has something wrong." );
				}

				logger.log( LogLevel.INFO, "RunCommandAdapter: Finished to run the command {}.", command );
			} );
		} );
	}
}
