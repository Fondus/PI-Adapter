package tw.fondus.fews.adapter.pi.aws.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The integration test of ImportFromS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ImportFromS3AdapterTest {
	private static final String HOST = "http://localhost:9000";
	private static final String BUCKET = "demo-bucket";
	private static final String OBJECT = "test";
	private static final String USERNAME = "";
	private static final String PASSWORD = "";
	private static final String OUTPUT = "src/test/resources/Output/Output.txt";

	@BeforeAll
	public static void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"--host",
				HOST,
				"--bucket",
				BUCKET,
				"--object",
				OBJECT,
				"-i",
				"",
				"-o",
				"Output.txt",
				"-us",
				USERNAME,
				"-pw",
				PASSWORD
		};

		S3Arguments arguments = S3Arguments.instance();
		new ImportFromS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assertions.assertTrue( PathUtils.isExists( OUTPUT ) );

		List<String> inputs = PathReader.readAllLines( "src/test/resources/Input/Upload.txt" );
		List<String> outputs = PathReader.readAllLines( OUTPUT );
		IntStream.range( 0, inputs.size() ).forEach( i -> Assertions.assertEquals( inputs.get( i ), outputs.get( i ) ) );
	}

	@AfterAll
	public static void after(){
		PathUtils.deleteIfExists( OUTPUT );
	}
}
