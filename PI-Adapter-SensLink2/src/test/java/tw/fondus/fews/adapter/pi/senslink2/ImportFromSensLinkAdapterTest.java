package tw.fondus.fews.adapter.pi.senslink2;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.senslink2.util.RunArguments;

/**
 * Unit test of Model adapter for import data from the SensLink 2.0.
 * 
 * @author Brad Chen
 *
 */
public class ImportFromSensLinkAdapterTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"D:\\SensLink",
				"-t",
				"201808161100",
				"-ti",
				"0",
				"-d",
				"1",
				"-i",
				"Waterlevels.xml",
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
