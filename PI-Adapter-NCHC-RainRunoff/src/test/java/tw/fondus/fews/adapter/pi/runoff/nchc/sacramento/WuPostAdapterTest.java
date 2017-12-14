package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Unit test of Model post-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuPostAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Sacramento",
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
		new WuSacramentoPostAdapter().execute(args, arguments);
	}
	
}
