package tw.fondus.fews.adapter.pi.aws.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;

import java.nio.file.Path;
import java.util.List;

/**
 * The integration test of ImportFromS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ImportFolderFromS3AdapterTest {
	private static final String HOST = "http://localhost:9000";
	private static final String BUCKET = "fews-taiwan";
	private static final String USERNAME = "minio";
	private static final String PASSWORD = "minio123";

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
				"Inundation/NC/"
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
	public static void after(){
		PathUtils.clean( "src/test/resources/Output" );
	}
}
