package tw.fondus.fews.adapter.pi.loss.richi;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.loss.richi.argument.ProcessArguments;

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
		
		ProcessArguments arguments = ProcessArguments.instance();
		new DisasterLossAdapter().execute(args, arguments);
	}
}
