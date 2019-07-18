package tw.fondus.fews.adapter.pi.senslink.v3;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.senslink.v3.ExportToSensLinkAdapter;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;

/**
 * Unit test of Model adapter for export data to the SensLink 3.0.
 * 
 * @author Brad Chen
 *
 */
public class ExportToSensLinkAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-ti",
				"0",
				"-d",
				"1",
				"-i",
				"Rainfall.xml",
				"-o",
				"",
				"-s",
				"1",
				"-us",
				"",
				"-pw",
				""
				};
		
		RunArguments arguments = new RunArguments();
		new ExportToSensLinkAdapter().execute(args, arguments);
	}

}
