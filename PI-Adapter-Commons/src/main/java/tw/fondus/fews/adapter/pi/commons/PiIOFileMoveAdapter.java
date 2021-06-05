package tw.fondus.fews.adapter.pi.commons;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.extend.PiIOMoveArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * FEWS adapter used to move data in PiIO folder with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class PiIOFileMoveAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		PiIOMoveArguments arguments = PiIOMoveArguments.instance();
		new PiIOFileMoveAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		PiIOMoveArguments modelArguments = this.asArguments( arguments, PiIOMoveArguments.class );
		if ( modelArguments.isSourceOutput() ){
			logger.log( LogLevel.INFO, " PiIOFileMoveAdapter: Start to move source: {} files into target: {}.", outputPath, inputPath );
			this.moveFiles( outputPath, inputPath );
			logger.log( LogLevel.INFO, " PiIOFileMoveAdapter: Finished to move source: {} files into target: {}.", outputPath, inputPath );
		} else {
			logger.log( LogLevel.INFO, " PiIOFileMoveAdapter: Start to move source: {} files into target: {}.", inputPath, outputPath );
			this.moveFiles( inputPath, outputPath );
			logger.log( LogLevel.INFO, " PiIOFileMoveAdapter: Finished to move source: {} files into target: {}.", inputPath, outputPath );
		}
	}

	/**
	 * Copy the files inside source into target folder.
	 *
	 * @param source source folder
	 * @param target target folder
	 */
	private void moveFiles( Path source, Path target ){
		try ( Stream<Path> paths = Files.list( source ) ) {
			paths.forEach( path -> {
				Path destPath = target.resolve( path.getFileName() );
				try {
					Files.move( path, destPath );
				} catch (IOException e) {
					this.getLogger().log( LogLevel.ERROR, "PiIOFileMoveAdapter: Move with source file: {} to target file: {} has IO problem.", path, destPath );
				}
			} );
		} catch ( IOException e ) {
			this.getLogger().log( LogLevel.ERROR, "PiIOFileMoveAdapter: Work with source: {} has IO problem.", source );
		}
	}
}
