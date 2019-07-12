package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

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
				"src/test/resources/Tank",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new TankPreAdapter().execute(args, arguments);
	}
	
}
