package tw.fondus.fews.adapter.pi.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import strman.Strman;
import tw.fondus.commons.cli.BasicCommandLineExecute;
import tw.fondus.commons.cli.argument.BasicArguments;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * A Parent class interface use to execute program with PI Command-Line Interface.
 * 
 * @author Brad Chen
 *
 */
@Slf4j
public abstract class PiCommandLineExecute extends BasicCommandLineExecute {
	private Path logPath;
	private Path diagnosticsFile;
	private PiDiagnostics diagnostics;
	
	@Override
	protected void run( BasicArguments arguments, Path basePath, Path inputPath, Path outputPath ) throws Exception {
		PiBasicArguments piArguments = (PiBasicArguments) arguments;
		
		// Check the diagnostics folder
		this.logPath = Paths.get( Strman.append( basePath.toString(), PATH, piArguments.getLogPath() ) );
		Prevalidated.checkExists( this.logPath, "PI-CommandLineExecute: The diagnostics directory not exist." );
		
		// Check the diagnostics file
		this.diagnosticsFile = Paths.get( Strman.append( this.logPath.toString(), PATH, piArguments.getDagnostics() ) );
		Prevalidated.checkExists( this.diagnosticsFile, "PI-CommandLineExecute: The diagnostics file not exist." );
		
		/** Manual log with PiDiagnostics **/
		this.diagnostics = XMLUtils.fromXML( this.diagnosticsFile, PiDiagnostics.class );
		this.diagnostics.clearMessages();
		
		/** Synchronous logger **/
		PiDiagnosticsLogger logger = PiDiagnosticsLogger.synchronous( log, this.diagnostics );
		
		try {
			this.adapterRun( piArguments, logger, basePath, inputPath, outputPath );
		} finally {
			if ( this.diagnostics.getMessages().size() == 0 ) {
				logger.log( LogLevel.WARN, "PI-CommandLineExecute: Synchronous diagnostics message faild, because program not logging!" );
			}
				
			try {
				XMLUtils.toXML( this.diagnosticsFile, this.diagnostics );
			} catch (Exception e) {
				logger.log( LogLevel.ERROR, "PI-CommandLineExecute: Write the diagnostics file has something wrong!" );
			}
		}
	}
	
	/**
	 * Execute adapter process arguments and working directory.
	 * 
	 * @param arguments
	 *            :The program arguments.
	 * @param logger
	 *            :The synchronous logger with slf4j and diagnostics.
	 * @param basePath
	 *            :The current working directory.
	 * @param inputPath
	 *            :The input directory path, relative to the current working directory.
	 * @param outputPath
	 *            :The output directory path, relative to the current working directory.
	 */
	protected abstract void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger,
			Path basePath, Path inputPath, Path outputPath );
}
