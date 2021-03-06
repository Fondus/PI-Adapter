package tw.fondus.fews.adapter.pi.trigrs;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.trigrs.argument.RunArguments;

/**
 * Unit test of Model executable-adapter for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
public class ExecuteTRIGRSTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/bin",
				"-e",
				"trigrs_64bit.exe",
				"-id",
				"../Input/",
				"-od",
				"../Output/",
				"-ld",
				"../Diagnostics/"
				};
		
		RunArguments arguments = RunArguments.instance();
		new TRIGRSExecutable().execute(args, arguments);
	}
}
