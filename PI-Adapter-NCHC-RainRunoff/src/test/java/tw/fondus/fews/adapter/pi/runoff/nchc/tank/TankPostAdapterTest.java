package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Unit test of Model post-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankPostAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Tank",
				"-i",
				"Time.DAT",
				"-o",
				"Output.xml",
				"-p",
				"Q.simulated",
				"-u",
				"Discharge (m³/s)"
				};
		
		PiArguments arguments = new PiArguments();
		new TankPostAdapter().execute(args, arguments);
	}
	
}
