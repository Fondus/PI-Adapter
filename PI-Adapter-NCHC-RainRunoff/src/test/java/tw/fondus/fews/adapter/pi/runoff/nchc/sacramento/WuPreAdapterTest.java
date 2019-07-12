package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

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
				"src/test/resources/Sacramento",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new WuSacramentoPreAdapter().execute( args, arguments );
	}
	
}
