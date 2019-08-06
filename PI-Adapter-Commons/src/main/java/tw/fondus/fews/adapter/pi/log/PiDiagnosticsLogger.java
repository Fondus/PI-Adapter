package tw.fondus.fews.adapter.pi.log;

import org.slf4j.Logger;

import lombok.NonNull;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;

/**
 * The logger is used to synchronous log with slf4j and diagnostics message to Delft-FEWS system.
 * 
 * @author Brad Chen
 *
 */
public class PiDiagnosticsLogger {
	private PiDiagnostics diagnostics;
	private Logger logger;
	
	private PiDiagnosticsLogger( @NonNull Logger logger, @NonNull PiDiagnostics diagnostics ) {
		this.logger = logger;
		this.diagnostics = diagnostics;
	}
	
	/**
	 * Synchronous log with slf4j logger and diagnostics.
	 * 
	 * @param level
	 * @param message
	 * @param variables
	 */
	public void log( LogLevel level, String message, String... variables ) {
		String logMessage = PiDiagnosticsLogger.buildMessage( message, variables );
		this.log( level, logMessage );
		this.diagnostics.addMessage( level.value(), logMessage );
	}
	
	/**
	 * Logging with slf4j logger.
	 * 
	 * @param level
	 * @param message
	 */
	private void log( LogLevel level, String message ) {
		Prevalidated.checkNonNull( level, "PiDiagnosticsLogger: LogLevel." );
		switch ( level ) {
			case DEBUG:
				this.logger.debug( message );
				break;
			case INFO:
				this.logger.info( message );		
				break;
			case WARN:
				this.logger.warn( message );
				break;
			case ERROR:
				this.logger.error( message );
				break;
			default:
				this.logger.trace( message );
				break;
		}
	}
	
	/**
	 * Replace the message {} with variables.
	 * 
	 * @param message
	 * @param variables
	 * @return
	 */
	private static String buildMessage( String message, String... variables ) {
		String temp = Prevalidated.checkNonNull( message, "PiDiagnosticsLogger: message." );
		for ( int i = 0; i < variables.length; i++ ){
			temp = temp.replaceFirst( "\\{\\}", variables[i] );
		}
		return temp;
	}
	
	/**
	 * Synchronous the slf4j logger and diagnostics.
	 * 
	 * @param logger
	 * @param diagnostics
	 * @return
	 */
	public static PiDiagnosticsLogger synchronous( Logger logger, PiDiagnostics diagnostics ) {
		return new PiDiagnosticsLogger( logger, diagnostics );
	}
}
