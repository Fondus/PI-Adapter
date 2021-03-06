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
 * FEWS adapter used for import data from the S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ImportFromS3Adapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3Arguments arguments = S3Arguments.instance();
		new ImportFromS3Adapter().execute( args, arguments );
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
		logger.log( LogLevel.INFO, "S3 Import Adapter: The target object with prefix is: {}.", object );

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();

		Path output = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
		try {
			logger.log( LogLevel.INFO, "S3 Import Adapter: Start to download object: {} with S3 API.", object );
			if ( client.isExistsBucket() ) {
				S3ProcessUtils.downloadS3Object( "S3 Import Adapter", logger, client, object, output );
			} else {
				logger.log( LogLevel.WARN, "S3 Import Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
			logger.log( LogLevel.INFO, "S3 Import Adapter: Finished to download object: {} with S3 API." );
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Import Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
