package tw.fondus.fews.adapter.pi.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.example.argument.ExecutableArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * The Model executable-adapter for running example model from Delft-FEWS. <br/>
 * Model basic command: java -jar Model.jar Input/TestLocation-input.txt Output/output.txt
 * 
 * @author Brad Chen
 *
 */
public class ExampleExecutable extends PiCommandLineExecute {
	
	public static void main(String[] args) {
		ExecutableArguments arguments = new ExecutableArguments();
		new ExampleExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		ExecutableArguments modelArguments = (ExecutableArguments) arguments;
		
		String executable = Strman.append( basePath.toString(), PATH, modelArguments.getExecutable() );
		String modelInput = Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0) );
		String modelOutput = Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get(0) );
		
		try {
			logger.log( LogLevel.INFO, "Example ExecutableAdapter: Start execute model.");
			
			// Run the model
			Executions.execute( Executions.SETTING_NOTHING,
					"java", "-jar", executable, modelInput, modelOutput );
			
			logger.log( LogLevel.INFO, "Example ExecutableAdapter: Finished execute model.");
			
		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
			logger.log( LogLevel.ERROR, "Example ExecutableAdapter: The adapter has something wrong!" );
		}
	}
	
}
