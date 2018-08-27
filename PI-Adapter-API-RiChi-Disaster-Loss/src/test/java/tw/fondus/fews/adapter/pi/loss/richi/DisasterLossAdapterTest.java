package tw.fondus.fews.adapter.pi.loss.richi;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.fews.adapter.pi.loss.richi.util.MapStacksArguments;

public class DisasterLossAdapterTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"/src/test/resources/",
				"-m",
				"mapstacks.xml"
				};
		
		PiBasicArguments arguments = new MapStacksArguments();
		new DisasterLossAdapter().execute(args, arguments);
	}
}
