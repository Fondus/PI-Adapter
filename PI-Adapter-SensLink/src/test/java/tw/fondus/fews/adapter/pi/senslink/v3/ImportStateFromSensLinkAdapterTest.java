package tw.fondus.fews.adapter.pi.senslink.v3;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;

/**
 * Unit test of Model adapter for import state data from the SensLink 3.0.
 *
 * @author Brad Chen
 *
 */
public class ImportStateFromSensLinkAdapterTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-t",
				"201810021600",
				"-ti",
				"0",
				"-d",
				"1",
				"-i",
				"Input.xml",
				"-o",
				"Output.xml",
				"-p",
				"State.switch",
				"-u",
				"m",
				"-us",
				"",
				"-pw",
				""
		};

		RunArguments arguments = new RunArguments();
		new ImportStateFromSensLinkAdapter().execute(args, arguments);
	}
}
