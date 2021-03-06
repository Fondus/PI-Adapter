package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The integration test of ImportFromS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ImportFolderFromS3AdapterTest extends SetUpS3Test {
	@BeforeAll
	public static void run() throws MinioException, IOException {
		if ( client.isNotExistsObject( PREFIX + OBJECT ) ){
			client.uploadObject( PREFIX + OBJECT, PathUtils.path( "src/test/resources/Input/Upload.txt" ) );
		}

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
				PREFIX
		};

		S3FolderArguments arguments = S3FolderArguments.instance();
		new ImportFolderFromS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		List<Path> paths = PathUtils.list( "src/test/resources/Output" );
		Assertions.assertFalse( paths.isEmpty() );
	}

	@AfterAll
	public static void after() throws MinioException {
		PathUtils.clean( "src/test/resources/Output" );
		client.removeObject( PREFIX + OBJECT );
	}
}
