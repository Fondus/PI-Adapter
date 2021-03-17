package tw.fondus.fews.adapter.pi.aws.storge;

import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * FEWS adapter used for import data from the S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ImportFromS3Adapter extends PiCommandLineExecute {
	public static void main( String[] args ){

	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {

	}
}
