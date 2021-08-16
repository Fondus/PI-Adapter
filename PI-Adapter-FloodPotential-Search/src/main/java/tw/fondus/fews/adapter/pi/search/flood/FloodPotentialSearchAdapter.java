package tw.fondus.fews.adapter.pi.search.flood;

import com.google.common.base.Preconditions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.search.flood.argument.FloodPotentialSearchArguments;

import java.nio.file.Path;

/**
 * Adapter for running search flood potential with feature value wth Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class FloodPotentialSearchAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		FloodPotentialSearchArguments arguments = FloodPotentialSearchArguments.instance();
		new FloodPotentialSearchAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		FloodPotentialSearchArguments modelArguments = this.asArguments( arguments, FloodPotentialSearchArguments.class );

		Preconditions.checkState( modelArguments.getInputs().size() == 1,
				"FloodPotentialSearchAdapter: The input.xml not give by command." );

		// Prepare the file
		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"FloodPotentialSearchAdapter: The input.xml do not exists!" );
	}
}
