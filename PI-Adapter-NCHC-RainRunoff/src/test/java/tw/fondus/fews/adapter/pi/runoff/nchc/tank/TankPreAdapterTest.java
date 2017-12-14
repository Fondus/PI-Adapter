package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Unit test of Model pre-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankPreAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Tank",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiArguments arguments = new PiArguments();
		new TankPreAdapter().execute(args, arguments);
	}
	
}
