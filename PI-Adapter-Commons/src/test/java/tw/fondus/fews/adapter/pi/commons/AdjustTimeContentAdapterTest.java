package tw.fondus.fews.adapter.pi.commons;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of AdjustTimeContentAdapter.
 *
 * @author Brad Chen
 *
 */
public class AdjustTimeContentAdapterTest {

	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-od",
				"Input/",
				"-i",
				"Input.xml,Base.xml",
				"-o",
				"Output.xml"
		};

		PiIOArguments arguments = new PiIOArguments();
		new AdjustTimeContentAdapter().execute( args, arguments );
	}
}
