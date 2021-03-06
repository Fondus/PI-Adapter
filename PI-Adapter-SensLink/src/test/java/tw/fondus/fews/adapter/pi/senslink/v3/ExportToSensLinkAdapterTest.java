package tw.fondus.fews.adapter.pi.senslink.v3;

import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;

/**
 * Unit test of Model adapter for export data to the SensLink 3.0.
 * 
 * @author Brad Chen
 *
 */
public class ExportToSensLinkAdapterTest {

	@Before
	public void run() {
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
				"-us",
				"",
				"-pw",
				""
				};
		
		RunArguments arguments = RunArguments.instance();
		new ExportToSensLinkAdapter().execute( args, arguments );
	}

	@Test
	public void test() {
		// to do
	}
}
