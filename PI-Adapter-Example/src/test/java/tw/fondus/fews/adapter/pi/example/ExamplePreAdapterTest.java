package tw.fondus.fews.adapter.pi.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The unit test of ExamplePreAdapter.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePreAdapterTest {

	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Input.xml",
				"-o",
				""
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new ExamplePreAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		List<String> lines = Files.readAllLines( Paths.get( "src/test/resources/Input/TestLocation.txt" ) );

		String content = "201907041000,0.12970397\n" + "201907041100,0.7155926\n" + "201907041200,0.44737074\n"
				+ "201907041300,0.87245566\n" + "201907041400,0.5827064\n" + "201907041500,0.1401003\n"
				+ "201907041600,0.7398107\n" + "201907041700,0.30581763";

		Assert.assertEquals( content, lines.stream().collect( Collectors.joining( "\n" )) );
	}
}
