package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model post-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankPostAdapterTest {

//	@Test
	public void test() {
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
		
		PiIOArguments arguments = new PiIOArguments();
		new TankPostAdapter().execute(args, arguments);
	}
	
}
