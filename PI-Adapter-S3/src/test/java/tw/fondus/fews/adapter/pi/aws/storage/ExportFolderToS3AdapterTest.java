package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;

/**
 * The integration test of ExportToS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ExportFolderToS3AdapterTest {
	private static final String HOST = "http://localhost:9000";
	private static final String BUCKET = "demo-bucket";
	private static final String USERNAME = "";
	private static final String PASSWORD = "";

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
				"Inundation/NC/",
				"--bucket-create"
		};

		S3FolderArguments arguments = S3FolderArguments.instance();
		new ExportFolderToS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( HOST )
						.credentials( USERNAME, PASSWORD )
						.build() )
				.defaultBucket( BUCKET )
				.build();

		Assertions.assertFalse( client.listObjects( "Inundation/NC/" ).isEmpty() );
	}

	@AfterAll
	public static void after(){

	}
}
