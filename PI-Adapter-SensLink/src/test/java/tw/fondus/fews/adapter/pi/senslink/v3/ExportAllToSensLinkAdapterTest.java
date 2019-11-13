package tw.fondus.fews.adapter.pi.senslink.v3;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.ExportArguments;

/**
 * Unit test of Model adapter for export all data to the SensLink 3.0.
 *
 * @author Brad Chen
 *
 */
public class ExportAllToSensLinkAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml",
				"-o",
				"",
				"-us",
				"",
				"-pw",
				""
		};

		ExportArguments arguments = new ExportArguments();
		new ExportAllToSensLinkAdapter().execute(args, arguments);
	}
}
