package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * FEWS adapter used to export folder data to S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ExportFolderToS3Adapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3FolderArguments arguments = S3FolderArguments.instance();
		new ExportFolderToS3Adapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		S3FolderArguments modelArguments = this.asArguments( arguments, S3FolderArguments.class );
		String host = modelArguments.getHost();
		String bucket = modelArguments.getBucket();
		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();
		String prefix = modelArguments.getObjectPrefix();

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();

		try {
			if ( client.isNotExistsBucket() && modelArguments.isCreate() ){
				logger.log( LogLevel.INFO, "S3 Export Folder Adapter: The target bucket: {} created by adapter.", bucket );
				client.createBucket();
			}

			logger.log( LogLevel.INFO, "S3 Export Folder Adapter: Start to upload folder: {} to prefix: {} with S3 API.", inputPath, prefix );
			if ( client.isExistsBucket() ) {
				List<Path> paths = PathUtils.list( inputPath );
				paths.forEach( path -> {
					String object = prefix + PathUtils.getName( path );
					try {
						client.uploadObject( object, path );
					} catch (MinioException | IOException e) {
						logger.log( LogLevel.ERROR, "S3 Export Adapter: Upload object: {} with S3 API has IOException! {}", object, e );
					}
				} );
			} else {
				logger.log( LogLevel.WARN, "S3 Export Folder Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
			logger.log( LogLevel.INFO, "S3 Export Folder Adapter: Finished to upload folder: {} to prefix: {} with S3 API.", inputPath, prefix );
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Export Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
