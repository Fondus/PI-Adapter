package tw.fondus.fews.adapter.pi.nc;

import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * The model adapter use to merge the test grid type of NetCDF, let can be import to Delft-FEWS.
 *
 * @author Brad Chen
 * @since 3.0.0
 */
public class TestGridMergeAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PiIOArguments arguments = PiIOArguments.instance();
		new TestGridMergeAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PiIOArguments modelArguments = this.asIOArguments( arguments );


	}
}
