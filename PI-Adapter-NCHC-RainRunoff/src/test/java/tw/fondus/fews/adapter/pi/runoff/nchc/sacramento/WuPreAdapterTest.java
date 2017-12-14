package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Unit test of Model pre-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuPreAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Sacramento",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiArguments arguments = new PiArguments();
		new WuSacramentoPreAdapter().execute(args, arguments);
	}
	
}
