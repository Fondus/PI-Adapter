package tw.fondus.fews.adapter.pi.trigrs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.trigrs.argument.RunArguments;

/**
 * Model executable-adapter for running TRIGRS landslide model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TRIGRSExecutable extends PiCommandLineExecute {
	
	public static void main(String[] args){
		RunArguments arguments = new RunArguments();
		new TRIGRSExecutable().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		String executeModel = modelArguments.getExecutable();
		String command = Strman.append( basePath.toString(), PATH, executeModel );
		
		logger.log( LogLevel.INFO, "TRIGRS Executable Adapter: Start TRIGRS simulation." );
		
		try {
			Executions.execute( executor -> executor.directory( basePath.toFile() ),
					command );
		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
			logger.log( LogLevel.ERROR, "TRIGRS Executable Adapter: Running TRIGRS simulation has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "TRIGRS Executable Adapter: Finished TRIGRS simulation." );
	}
}
