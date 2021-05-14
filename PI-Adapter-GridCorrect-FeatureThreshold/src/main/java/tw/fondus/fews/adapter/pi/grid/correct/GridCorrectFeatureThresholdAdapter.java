package tw.fondus.fews.adapter.pi.grid.correct;

import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * Adapter for running correct grid with feature threshold wth Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class GridCorrectFeatureThresholdAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){

	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {

	}
}
