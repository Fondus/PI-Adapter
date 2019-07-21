package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model post-adapter for running NCHC MSFRM model.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPostAdapterTest {

//	@Test
	public void test() {
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
		
		PiIOArguments arguments = new PiIOArguments();
		new MSFRMPostAdapter().execute(args, arguments);
	}
	
}
