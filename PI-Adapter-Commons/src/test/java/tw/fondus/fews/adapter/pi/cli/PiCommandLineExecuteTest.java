package tw.fondus.fews.adapter.pi.cli;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * The unit test of PiCommandLineExecute.
 * 
 * @author Brad Chen
 *
 */
public class PiCommandLineExecuteTest {
	@Getter
	private String[] args;
	
	@Before
	public void setUp() {
		this.args = new String[]{
				"-b",
				"src/test/resources",
				"-t",
				"201808201600"
				};
	}
	
	@Test
	public void test() {
		PiBasicArguments arguments = PiBasicArguments.instance();
		new TestCLI().execute( this.getArgs(), arguments );
	}
	
	/**
	 * Test implements with PiCommandLineExecute.
	 * 
	 * @author Brad Chen
	 *
	 */
	private static class TestCLI extends PiCommandLineExecute {
		@Override
		protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
				Path outputPath ) {
			Assert.assertTrue( arguments.getTimeZero().isBeforeNow() );
			logger.log( LogLevel.INFO, "The PI-CommandLineExecute unit test {}.", 1 );
		}
	}
}
