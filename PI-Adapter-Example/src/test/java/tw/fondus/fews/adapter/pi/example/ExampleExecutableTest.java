package tw.fondus.fews.adapter.pi.example;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.example.argument.ExecutableArguments;

/**
 * The unit test of ExampleExecutable.
 * 
 * @author Brad Chen
 *
 */
public class ExampleExecutableTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"TestLocation.txt",
				"-o",
				"TestLocation-output.txt",
				"-e",
				"Model.jar"
				};
		
		ExecutableArguments arguments = new ExecutableArguments();
		new ExampleExecutable().execute( args, arguments );
	}
}
