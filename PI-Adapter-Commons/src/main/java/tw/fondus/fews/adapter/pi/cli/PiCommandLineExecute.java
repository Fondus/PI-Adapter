package tw.fondus.fews.adapter.pi.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tw.fondus.commons.cli.BasicCommandLineExecute;
import tw.fondus.commons.cli.argument.BasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.logger.PiDiagnosticsUtils;

import java.nio.file.Path;

/**
 * A Parent class interface use to execute program with PI command-line interface.
 * 
 * @author Brad Chen
 *
 */
@Slf4j
public abstract class PiCommandLineExecute extends BasicCommandLineExecute {
	@Getter
	private PiDiagnosticsLogger logger;

	@Override
	protected void run( BasicArguments arguments, Path basePath, Path inputPath, Path outputPath ) {
		PiBasicArguments piArguments = (PiBasicArguments) arguments;

		// Synchronous logger
		this.logger = PiDiagnosticsUtils.initializeLogger( basePath, piArguments.getLogPath(),
				piArguments.getDiagnostics() );

		PiDiagnosticsUtils.adapterProcessInLoggerScope( this.logger, () -> this.adapterRun( piArguments, logger, basePath, inputPath, outputPath ) );
	}
	
	/**
	 * Execute adapter process with arguments.
	 * 
	 * @param arguments the adapter arguments.
	 * @param logger the synchronous logger with slf4j and diagnostics.
	 * @param basePath current working directory.
	 * @param inputPath input directory path, relative to the current working directory.
	 * @param outputPath output directory path, relative to the current working directory.
	 */
	protected abstract void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger,
			Path basePath, Path inputPath, Path outputPath );

	/**
	 * Cast the basic arguments to IO arguments type.
	 *
	 * @param arguments basic arguments
	 * @return IO type arguments
	 * @since 3.0.0
	 */
	protected PiIOArguments asIOArguments( PiBasicArguments arguments ){
		return this.asArguments( arguments, PiIOArguments.class );
	}

	/**
	 * Cast the basic arguments to target type.
	 *
	 * @param arguments basic arguments
	 * @param clazz class of T
	 * @param <T> the target arguments type
	 * @return the target type arguments
	 * @since 3.0.0
	 */
	@SuppressWarnings( "unchecked" )
	protected <T extends PiBasicArguments> T asArguments( PiBasicArguments arguments, Class<T> clazz ){
		return (T) arguments;
	}
}
