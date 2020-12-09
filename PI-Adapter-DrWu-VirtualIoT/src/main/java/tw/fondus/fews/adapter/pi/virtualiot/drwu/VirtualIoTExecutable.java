package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.ExecutableArguments;

/**
 * Model executable for running Virtual IoT model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class VirtualIoTExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ExecutableArguments arguments = ExecutableArguments.instance();
		new VirtualIoTExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		ExecutableArguments executableArguments = this.asArguments( arguments, ExecutableArguments.class );

		logger.log( LogLevel.INFO, "VirtualIotExecutable: Starting executable process." );
		Path templatePath = Prevalidated.checkExists( basePath.resolve( executableArguments.getTemplateDir() ),
				"VirtualIotExecutable: Can not find template directory." );

		Path basinTemplatePath = Prevalidated.checkExists(
				templatePath.resolve( "Basin" ).resolve( executableArguments.getBasin() ),
				"VirtualIotExecutable: Can not find template directory of basin." );

		Path executablePath = Prevalidated.checkExists( basePath.resolve( executableArguments.getExecutableDir() ),
				"VirtualIotExecutable: Can not find executable directory." );

		logger.log( LogLevel.INFO, "VirtualIotExecutable: Clean executable directory and copy template file." );
		PathUtils.clean( executablePath );
		PathUtils.copy( inputPath.resolve( executableArguments.getInputs().get( 0 ) ), executablePath );
		PathUtils.copiesWithoutSubDirectory( templatePath, executablePath );
		PathUtils.copiesFileFlattenWithSubDirectory( basinTemplatePath, executablePath );

		try {
			logger.log( LogLevel.INFO, "VirtualIotExecutable: Running model." );
			String command = executablePath.resolve( executableArguments.getExecutable().get( 0 ) )
					.toAbsolutePath()
					.toString();
			Executions.execute( executor -> executor.directory( executablePath.toFile() ), command );
		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
			logger.log( LogLevel.ERROR, "VirtualIotExecutable: Running model has something wrong." );
		}
		
		logger.log( LogLevel.INFO, "VirtualIotExecutable: Finished executable process." );
	}
}
