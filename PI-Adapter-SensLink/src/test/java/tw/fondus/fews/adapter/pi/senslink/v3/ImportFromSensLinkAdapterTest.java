package tw.fondus.fews.adapter.pi.senslink.v3;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.senslink.v3.ImportFromSensLinkAdapter;
import tw.fondus.fews.adapter.pi.senslink.v3.util.RunArguments;

/**
 * Unit test of Model adapter for import data from the SensLink 3.0.
 * 
 * @author Brad Chen
 *
 */
public class ImportFromSensLinkAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\SensLink",
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
				"H.obs",
				"-u",
				"m",
				"-us",
				"",
				"-pw",
				""
				};
		
		RunArguments arguments = new RunArguments();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}

}
