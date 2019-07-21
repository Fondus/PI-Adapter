package tw.fondus.fews.adapter.pi.example;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of ExamplePreAdapter.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePreAdapterTest {
	
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Input.xml",
				"-o",
				""
				};
		
		PiIOArguments arguments = new PiIOArguments();
		new ExamplePreAdapter().execute( args, arguments );
	}
}
