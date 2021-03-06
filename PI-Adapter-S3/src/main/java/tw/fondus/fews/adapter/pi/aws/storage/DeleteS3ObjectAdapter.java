package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;
import tw.fondus.fews.adapter.pi.aws.storage.util.S3ProcessUtils;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;

/**
 * FEWS adapter used to delete data to S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class DeleteS3ObjectAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3Arguments arguments = S3Arguments.instance();
		new DeleteS3ObjectAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		S3Arguments modelArguments = this.asArguments( arguments, S3Arguments.class );

		String host = modelArguments.getHost();
		String bucket = modelArguments.getBucket();
		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();
		String object = modelArguments.getObjectPrefix() + modelArguments.getObject();

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();

		logger.log( LogLevel.INFO, "S3 Delete Adapter: The adapter process try to delete object: {} inside target bucket: {}.", object, bucket );
		try {
			if ( client.isNotExistsBucket() ) {
				logger.log( LogLevel.WARN, "S3 Delete Adapter: The target bucket: {} not exist, will skip process.", bucket );
			} else {
				S3ProcessUtils.deleteS3Object( "S3 Delete Adapter", logger, client, object );
			}
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Delete Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
