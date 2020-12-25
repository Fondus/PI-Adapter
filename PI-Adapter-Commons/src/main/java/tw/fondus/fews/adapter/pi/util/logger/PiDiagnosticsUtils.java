package tw.fondus.fews.adapter.pi.util.logger;

import lombok.extern.slf4j.Slf4j;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The tool used to operation with diagnostics logger.
 *
 * @author Brad Chen
 * @since 3.0.0
 */
@Slf4j
public class PiDiagnosticsUtils {
	private PiDiagnosticsUtils(){}

	/**
	 * Initialize the diagnostics logger.
	 *
	 * @param basePath current working directory
	 * @param logFolder logging folder path, relative to the current working directory
	 * @param diagnosticsName logging file inside log path
	 * @return diagnostics logger
	 */
	public static PiDiagnosticsLogger initializeLogger( Path basePath, String logFolder, String diagnosticsName ){
		// Check the diagnostics folder
		Path logPath = Prevalidated.checkExists( basePath.resolve( logFolder ),
				"PiDiagnostics: The diagnostics directory not exist." );

		// Check the diagnostics file
		Path diagnosticsPath = Prevalidated.checkExists( logPath.resolve( diagnosticsName ),
				"PiDiagnostics: The diagnostics file not exist." );

		// Load the FEWS log message XML.
		PiDiagnostics diagnostics = null;
		try {
			diagnostics = XMLUtils.fromXML( diagnosticsPath, PiDiagnostics.class );
			diagnostics.clearMessages();
		} catch ( Exception e ){
			log.error( "PiDiagnostics: Read Pi-Diagnostics file has something wrong.", e );
		}

		// Synchronous logger
		return PiDiagnosticsLogger.synchronous( log, diagnostics, diagnosticsPath );
	}

	/**
	 * Running adapter process in diagnostics logger scope.
	 *
	 * @param logger diagnostics logger
	 * @param adapterProcess adapter process
	 */
	public static void adapterProcessInLoggerScope( PiDiagnosticsLogger logger, Runnable adapterProcess ){
		try {
			Objects.requireNonNull( adapterProcess ).run();
		} finally {
			PiDiagnostics diagnostics = logger.getDiagnostics();
			Optional.ofNullable( diagnostics ).ifPresent( piDiagnostics -> {
				if ( piDiagnostics.getMessages().size() == 0 ) {
					logger.log( LogLevel.WARN, "PiDiagnostics: Synchronous diagnostics with empty message, because adapter not has do any logging." );
				}

				try {
					XMLUtils.toXML( logger.getPath(), piDiagnostics );
				} catch (Exception e) {
					logger.log( LogLevel.ERROR, "PiDiagnostics: Write the diagnostics bean into file has something wrong!" );
				}
			} );
		}
	}
}
