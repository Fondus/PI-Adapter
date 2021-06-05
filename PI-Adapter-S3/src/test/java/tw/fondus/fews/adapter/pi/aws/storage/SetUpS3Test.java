package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import tw.fondus.commons.minio.MinioHighLevelClient;

/**
 * The class used to prepare the S3 test.
 *
 * @author Brad Chen
 *
 */
public abstract class SetUpS3Test {
	protected static final String HOST = "http://localhost:9000";
	protected static final String BUCKET = "demo-bucket";
	protected static final String OBJECT = "Upload.txt";
	protected static final String PREFIX = "Inundation/NC/";
	protected static final String USERNAME = "";
	protected static final String PASSWORD = "";
	protected static final MinioHighLevelClient client;

	static {
		client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( HOST )
						.credentials( USERNAME, PASSWORD )
						.build() )
				.defaultBucket( BUCKET )
				.build();
	}
}
