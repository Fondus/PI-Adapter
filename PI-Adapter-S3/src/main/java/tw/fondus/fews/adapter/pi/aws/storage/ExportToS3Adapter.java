package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * FEWS adapter used to export data to S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ExportToS3Adapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3Arguments arguments = S3Arguments.instance();
		new ExportToS3Adapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		S3Arguments modelArguments = this.asArguments( arguments, S3Arguments.class );

		String host = modelArguments.getHost();
		String bucket = modelArguments.getBucket();
		String object = modelArguments.getObject();
		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();

		Path input = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"S3 Export Adapter: The input resource not exists!" );
		try {
			if ( client.isNotExistsBucket() && modelArguments.isCreate() ){
				logger.log( LogLevel.INFO, "S3 Export Adapter: The target bucket: {} created by adapter.", bucket );
				client.createBucket();
			}

			logger.log( LogLevel.INFO, "S3 Export Adapter: Start to upload object: {} with S3 API.", object );
			if ( client.isExistsBucket() ) {
				boolean state = client.uploadObject( object, input );
				if ( state ){
					logger.log( LogLevel.INFO, "S3 Export Adapter: Succeeded to upload object: {} with S3 API.", object );
				} else {
					logger.log( LogLevel.WARN, "S3 Export Adapter: Failed to upload object: {} with S3 API.", object );
				}
			} else {
				logger.log( LogLevel.WARN, "S3 Export Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
			logger.log( LogLevel.INFO, "S3 Export Adapter: Finished to upload object: {} with S3 API." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "S3 Export Adapter: Upload object: {} with S3 API has IOException! {}", object, e );
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Export Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
