package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The integration test of ImportFromS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ImportFromS3AdapterTest extends SetUpS3Test {
	private static final String OUTPUT = "src/test/resources/Output/Output.txt";

	@BeforeAll
	public static void run() throws MinioException, IOException {
		if ( client.isNotExistsObject( OBJECT ) ){
			client.uploadObject( OBJECT, PathUtils.path( "src/test/resources/Input/Upload.txt" ) );
		}

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
	public static void after() throws MinioException {
		PathUtils.deleteIfExists( OUTPUT );
		client.removeObject( OBJECT );
	}
}
