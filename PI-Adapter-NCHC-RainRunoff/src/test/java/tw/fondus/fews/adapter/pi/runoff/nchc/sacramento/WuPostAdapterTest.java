package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.PostAdapterTestCase;

import java.io.IOException;

/**
 * Unit test of Model post-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuPostAdapterTest extends PostAdapterTestCase {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/Sacramento",
				"-i",
				"Time.DAT",
				"-o",
				"Output.xml",
				"-p",
				"Q.simulated",
				"-u",
				"Discharge (m³/s)"
				};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new WuSacramentoPostAdapter().execute(args, arguments);

		this.setTestCase( "Sacramento" );
	}

	@Test
	public void test() throws IOException {
		this.testProcess();
	}
}
