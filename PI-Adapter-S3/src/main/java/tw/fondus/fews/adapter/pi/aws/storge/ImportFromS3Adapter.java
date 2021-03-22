package tw.fondus.fews.adapter.pi.aws.storge;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storge.argument.S3Arguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
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

		Path output = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
		try {
			logger.log( LogLevel.INFO, "S3 Import Adapter: Start to download object: {} with S3 API.", object );
			if ( client.isExistsBucket() ) {
				Path saved = client.getObjectAndSave( object, output );
				if ( PathUtils.isExists( saved ) ){
					logger.log( LogLevel.INFO, "S3 Import Adapter: Succeeded to download object: {} with S3 API.", object );
				} else {
					logger.log( LogLevel.WARN, "S3 Import Adapter: Failed to download object: {} with S3 API.", object );
				}
			} else {
				logger.log( LogLevel.WARN, "S3 Import Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
			logger.log( LogLevel.INFO, "S3 Import Adapter: Finished to download object: {} with S3 API." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "S3 Import Adapter: Download object: {} with S3 API has IOException! {}", object, e );
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Import Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
