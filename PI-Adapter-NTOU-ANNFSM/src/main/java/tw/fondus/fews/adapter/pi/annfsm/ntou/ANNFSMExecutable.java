package tw.fondus.fews.adapter.pi.annfsm.ntou;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.ExecutableArguments;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * Model executable for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class ANNFSMExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ExecutableArguments arguments = ExecutableArguments.instance();
		new ANNFSMExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ANNFSMExecutableAdapter: Starting ANNFSMExecutableAdapter process." );
		ExecutableArguments executableArguments = (ExecutableArguments) arguments;
		Path executablePath = Prevalidated.checkExists( basePath.resolve( executableArguments.getExecutableDir() ),
				"ANNFSMExecutableAdapter: The directory of executable is not exist." );

		try {
			logger.log( LogLevel.INFO, "ANNFSMExecutableAdapter: Coping model input file from input directory." );
			PathUtils.copy( inputPath.resolve( executableArguments.getInputs().get( 0 ) ), executablePath );
			PathUtils.copy( inputPath.resolve( executableArguments.getInputs().get( 1 ) ), executablePath );

			logger.log( LogLevel.INFO, "ANNFSMExecutableAdapter: Running mdoel." );
			String command = executablePath.resolve( executableArguments.getExecutable().get( 0 ) ).toString();
			Executions.execute( executor -> executor.directory( executablePath.toFile() ), command );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "ANNFSMExecutableAdapter: Coping file from input path has something wrong." );
		} catch (InvalidExitValueException e) {
			logger.log( LogLevel.ERROR, "ANNFSMExecutableAdapter: Running model has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "ANNFSMExecutableAdapter: Running model has something wrong." );
		} catch (TimeoutException e) {
			logger.log( LogLevel.ERROR, "ANNFSMExecutableAdapter: Running model has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "ANNFSMExecutableAdapter: Finished ANNFSMExecutableAdapter process." );
	}

}
