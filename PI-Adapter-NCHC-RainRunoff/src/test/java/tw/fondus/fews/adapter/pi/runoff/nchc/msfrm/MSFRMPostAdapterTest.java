package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Unit test of Model post-adapter for running NCHC MSFRM model.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPostAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_MSFRM",
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
		new MSFRMPostAdapter().execute(args, arguments);
	}
	
}
