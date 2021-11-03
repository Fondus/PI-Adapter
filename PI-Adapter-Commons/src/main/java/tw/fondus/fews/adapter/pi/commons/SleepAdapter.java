package tw.fondus.fews.adapter.pi.commons;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.extend.SleepArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * The commons adapter tool it used to sleep millisecond with FEWS.
 *
 * @author Brad Chen
 *
 */
public class SleepAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		SleepArguments arguments = SleepArguments.instance();
		new PiIOFileMoveAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		SleepArguments modelArguments = this.asArguments( arguments, SleepArguments.class );
		logger.log( LogLevel.INFO, "SleepAdapter: Sleep thread with {} millisecond.", modelArguments.getSleep() );
		try {
			Thread.sleep( modelArguments.getSleep() );
		} catch (InterruptedException e) {
			logger.log( LogLevel.WARN, "SleepAdapter: Interrupted sleep thread by external process." );
		}
	}
}
