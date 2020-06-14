package tw.fondus.fews.adapter.pi.log;

import lombok.NonNull;
import org.slf4j.Logger;
import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;

import java.util.Objects;

/**
 * The logger is used to synchronous log with slf4j and diagnostics message to Delft-FEWS system.
 * 
 * @author Brad Chen
 *
 */
public class PiDiagnosticsLogger {
	private final PiDiagnostics diagnostics;
	private final Logger logger;
	
	private PiDiagnosticsLogger( @NonNull Logger logger, @NonNull PiDiagnostics diagnostics ) {
		this.logger = logger;
		this.diagnostics = diagnostics;
	}
	
	/**
	 * Synchronous log with slf4j logger and diagnostics.
	 * 
	 * @param level log level
	 * @param message log message
	 * @param variables the variables used to log
	 */
	public void log( LogLevel level, String message, Object... variables ) {
		String logMessage = PiDiagnosticsLogger.buildMessage( message, variables );
		this.slf4j( level, logMessage );
		this.diagnostics.addMessage( level.value(), logMessage );
	}
	
	/**
	 * Logging with slf4j logger.
	 * 
	 * @param level log level
	 * @param message log message
	 */
	private void slf4j( LogLevel level, String message ) {
		Objects.requireNonNull( level, "PiDiagnosticsLogger: LogLevel." );
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
	 * @param pattern source message pattern
	 * @param variables the variables used to log
	 * @return format message
	 */
	private static String buildMessage( String pattern, Object... variables ) {
		if ( Strman.isBlank( pattern ) || Objects.isNull( variables ) || variables.length == 0 ){
			return pattern;
		}

		int patternLength = pattern.length();
		StringBuilder result = new StringBuilder( patternLength + 50 );

		int handledPosition = 0;
		int delimIndex;
		for ( Object variable : variables ) {
			delimIndex = pattern.indexOf( "{", handledPosition );
			if ( delimIndex == -1 ) {
				if ( handledPosition == 0 ) {
					return pattern;
				}
				result.append( pattern, handledPosition, patternLength );
				return result.toString();
			}

			result.append( pattern, handledPosition, delimIndex );
			result.append( variable.toString() );
			handledPosition = delimIndex + 2;
		}
		result.append( pattern, handledPosition, pattern.length() );
		return result.toString();
	}
	
	/**
	 * Synchronous the slf4j logger and diagnostics.
	 * 
	 * @param logger logger
	 * @param diagnostics diagnostics
	 * @return diagnostics logger
	 */
	public static PiDiagnosticsLogger synchronous( Logger logger, PiDiagnostics diagnostics ) {
		return new PiDiagnosticsLogger( logger, diagnostics );
	}
}
