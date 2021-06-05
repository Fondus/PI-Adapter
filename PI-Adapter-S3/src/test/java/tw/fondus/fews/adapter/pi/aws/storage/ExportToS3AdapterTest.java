package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;

/**
 * The integration test of ExportToS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ExportToS3AdapterTest extends SetUpS3Test {
	private static final String OBJECT = "Upload.txt";

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
				"Upload.txt",
				"-o",
				"",
				"-us",
				USERNAME,
				"-pw",
				PASSWORD
		};

		S3Arguments arguments = S3Arguments.instance();
		new ExportToS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assertions.assertTrue( client.isExistsObject( OBJECT ) );
	}

	@AfterAll
	public static void after() throws MinioException {
		client.removeObject( OBJECT );
	}
}
