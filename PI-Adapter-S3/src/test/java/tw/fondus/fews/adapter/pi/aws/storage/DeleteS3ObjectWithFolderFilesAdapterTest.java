package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;

import java.io.IOException;

/**
 * The unit test of DeleteS3ObjectWithFolderFilesAdapter.
 *
 * @author Brad Chen
 *
 */
@Disabled
public class DeleteS3ObjectWithFolderFilesAdapterTest extends SetUpS3Test {
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
				"PREFIX"
		};

		S3FolderArguments arguments = S3FolderArguments.instance();
		new DeleteS3ObjectWithFolderFilesAdapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assertions.assertTrue( client.isNotExistsObject( PREFIX + OBJECT ) );
	}
}
