package tw.fondus.fews.adapter.pi.loss.richi;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;

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
		
		PiBasicArguments arguments = new PiArguments();
		new DisasterLossAdapter().execute(args, arguments);
	}
}
