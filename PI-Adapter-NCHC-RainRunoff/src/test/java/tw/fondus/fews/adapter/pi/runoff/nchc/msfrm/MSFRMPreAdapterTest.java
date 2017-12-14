package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

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
				"\\RR_NCHC_MSFRM",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiArguments arguments = new PiArguments();
		new MSFRMPreAdapter().execute(args, arguments);
	}
	
}
