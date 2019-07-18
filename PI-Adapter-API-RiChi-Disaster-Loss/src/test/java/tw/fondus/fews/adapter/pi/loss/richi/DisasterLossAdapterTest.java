package tw.fondus.fews.adapter.pi.loss.richi;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of Disaster Loss Adapter.
 * 
 * @author Chao
 *
 */
public class DisasterLossAdapterTest {
	
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"Search.xml",
				"-o",
				""
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new DisasterLossAdapter().execute(args, arguments);
	}
}
