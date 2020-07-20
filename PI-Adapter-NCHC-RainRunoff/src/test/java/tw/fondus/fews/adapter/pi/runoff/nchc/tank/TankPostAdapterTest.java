package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.PostAdapterTestCase;

import java.io.IOException;

/**
 * Unit test of Model post-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankPostAdapterTest extends PostAdapterTestCase {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/Tank",
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
		new TankPostAdapter().execute(args, arguments);

		this.setTestCase( "Tank" );
	}

	@Test
	public void test() throws IOException {
		this.testProcess();
	}
}
