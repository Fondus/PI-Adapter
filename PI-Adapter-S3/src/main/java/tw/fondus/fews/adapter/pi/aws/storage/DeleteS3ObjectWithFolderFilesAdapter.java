package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;
import tw.fondus.fews.adapter.pi.aws.storage.util.S3ProcessUtils;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;
import java.util.List;

/**
 * FEWS adapter used to folder file names to delete data to S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class DeleteS3ObjectWithFolderFilesAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3FolderArguments arguments = S3FolderArguments.instance();
		new DeleteS3ObjectWithFolderFilesAdapter().execute( args, arguments );
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

		logger.log( LogLevel.INFO, "S3 Delete FolderFiles Adapter: The adapter process try to find delete objects inside target bucket: {} with folder: {}.", bucket, inputPath );
		try {
			if ( client.isNotExistsBucket() ) {
				logger.log( LogLevel.WARN, "S3 Delete FolderFiles Adapter: The target bucket: {} not exist, will skip process.", bucket );
			} else {
				List<Path> paths = PathUtils.list( inputPath );
				if ( paths.isEmpty() ){
					logger.log( LogLevel.WARN, "S3 Delete FolderFiles Adapter: The folder: {} is empty, will skip process.", inputPath );
				} else {
					paths.forEach( path -> {
						String object = prefix + PathUtils.getName( path );
						S3ProcessUtils.deleteS3Object( "S3 Delete FolderFiles Adapter", logger, client, object );
					} );
				}
			}
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Delete FolderFiles Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
