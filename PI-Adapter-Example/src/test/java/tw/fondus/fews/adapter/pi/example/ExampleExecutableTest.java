package tw.fondus.fews.adapter.pi.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.example.argument.ExecutableArguments;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The unit test of ExampleExecutable.
 * 
 * @author Brad Chen
 *
 */
public class ExampleExecutableTest {
	@Before
	public void run(){
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
		
		ExecutableArguments arguments = ExecutableArguments.instance();
		new ExampleExecutable().execute( args, arguments );
	}

	@Test
	public void test() {
		Assert.assertTrue( Files.exists( Paths.get( "src/test/resources/Output/TestLocation-output.txt" ) ) );
	}
}
