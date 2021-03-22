package tw.fondus.fews.adapter.pi.aws.storge;

import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.fews.adapter.pi.aws.storge.argument.S3Arguments;

/**
 * The integration test of ExportToS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ExportToS3AdapterTest {
	private static final String HOST = "http://localhost:9000";
	private static final String BUCKET = "demo-bucket";
	private static final String OBJECT = "test";
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
		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( HOST )
						.credentials( USERNAME, PASSWORD )
						.build() )
				.defaultBucket( BUCKET )
				.build();

		Assertions.assertTrue( client.isExistsObject( OBJECT ) );
	}

	@AfterAll
	public static void after(){
		// to do
	}
}
