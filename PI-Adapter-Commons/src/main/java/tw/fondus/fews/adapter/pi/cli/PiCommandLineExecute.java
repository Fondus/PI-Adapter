package tw.fondus.fews.adapter.pi.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tw.fondus.commons.cli.BasicCommandLineExecute;
import tw.fondus.commons.cli.argument.BasicArguments;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;
import java.util.Optional;

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

		// Check the diagnostics folder
		Path logPath = Prevalidated.checkExists( basePath.resolve( piArguments.getLogPath() ),
				"PI-CommandLineExecute: The diagnostics directory not exist." );
		
		// Check the diagnostics file
		Path diagnosticsFile = Prevalidated.checkExists( logPath.resolve( piArguments.getDiagnostics() ),
				"PI-CommandLineExecute: The diagnostics file not exist." );

		// Load the FEWS log message XML.
		PiDiagnostics diagnostics = null;
		try {
			diagnostics = XMLUtils.fromXML( diagnosticsFile, PiDiagnostics.class );
			diagnostics.clearMessages();
		} catch ( Exception e ){
			log.error( "PiCommandLineExecute: Read Pi-Diagnostics file has something wrong." );
		}

		// Synchronous logger
		this.logger = PiDiagnosticsLogger.synchronous( log, diagnostics );
		
		try {
			this.adapterRun( piArguments, logger, basePath, inputPath, outputPath );
		} finally {
			Optional.ofNullable( diagnostics ).ifPresent( piDiagnostics -> {
				if ( piDiagnostics.getMessages().size() == 0 ) {
					logger.log( LogLevel.WARN, "PI-CommandLineExecute: Synchronous diagnostics with empty message, because adapter not has do any logging." );
				}

				try {
					XMLUtils.toXML( diagnosticsFile, piDiagnostics );
				} catch (Exception e) {
					logger.log( LogLevel.ERROR, "PI-CommandLineExecute: Write the diagnostics bean into file has something wrong!" );
				}
			} );
		}
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
