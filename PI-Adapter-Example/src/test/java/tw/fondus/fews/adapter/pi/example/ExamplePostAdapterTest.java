package tw.fondus.fews.adapter.pi.example;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of ExamplePostAdapter.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePostAdapterTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"TestLocation-output.txt",
				"-o",
				"Output.xml",
				"-p",
				"H.simu",
				"-u",
				"m"
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new ExamplePostAdapter().execute( args, arguments );
	}
}
