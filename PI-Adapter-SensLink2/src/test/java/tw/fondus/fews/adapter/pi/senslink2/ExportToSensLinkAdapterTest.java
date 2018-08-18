package tw.fondus.fews.adapter.pi.senslink2;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.senslink2.util.RunArguments;

/**
 * Unit test of Model adapter for export data to the SensLink 2.0.
 * 
 * @author Brad Chen
 *
 */
public class ExportToSensLinkAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\SensLink",
				"-ti",
				"0",
				"-d",
				"1",
				"-i",
				"Rainfall.xml",
				"-o",
				"",
				"-us",
				"",
				"-pw",
				""
				};
		
		RunArguments arguments = new RunArguments();
		new ExportToSensLinkAdapter().execute(args, arguments);
	}

}
