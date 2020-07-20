package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.PostAdapterTestCase;

import java.io.IOException;

/**
 * Unit test of Model post-adapter for running NCHC MSFRM model.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPostAdapterTest extends PostAdapterTestCase {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/MSFRM",
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
		new MSFRMPostAdapter().execute(args, arguments);

		this.setTestCase( "MSFRM" );
	}

	@Test
	public void test() throws IOException {
		this.testProcess();
	}
}
