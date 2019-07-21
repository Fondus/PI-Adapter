package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model pre-adapter for running NCHC MSFRM model.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPreAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/MSFRM",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new MSFRMPreAdapter().execute(args, arguments);
	}
	
}
