package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;

/**
 * The integration test of ExportToS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ExportFolderToS3AdapterTest extends SetUpS3Test {
	private static final String PREFIX = "Inundation/NC/";

	@BeforeAll
	public static void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"--host",
				HOST,
				"--bucket",
				BUCKET,
				"-i",
				"",
				"-o",
				"",
				"-us",
				USERNAME,
				"-pw",
				PASSWORD,
				"--object-prefix",
				PREFIX,
				"--bucket-create",
				"--file-prefix",
				"",
				"--file-suffix",
				"txt"
		};

		S3FolderArguments arguments = S3FolderArguments.instance();
		new ExportFolderToS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assertions.assertFalse( client.listObjects( PREFIX ).isEmpty() );
	}

	@AfterAll
	public static void after() throws MinioException {
		client.removeObject( PREFIX + OBJECT );
	}
}
