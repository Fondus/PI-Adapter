package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * FEWS adapter used for import data in folder from the S3 API with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class ImportFolderFromS3Adapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		S3FolderArguments arguments = S3FolderArguments.instance();
		new ImportFolderFromS3Adapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		S3FolderArguments modelArguments = this.asArguments( arguments, S3FolderArguments .class );

		String host = modelArguments.getHost();
		String bucket = modelArguments.getBucket();
		String username = modelArguments.getUsername();
		String password = modelArguments.getPassword();
		String prefix = modelArguments.getObjectPrefix();
		logger.log( LogLevel.INFO, "S3 Import Folder Adapter: The target folder prefix is: {}.", prefix );

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();
		try {
			logger.log( LogLevel.INFO,
					"S3 Import Folder Adapter: Start to download objects with folder prefix: {} with S3 API.", prefix );
			if ( client.isExistsBucket() ) {
				List<Item> objects = client.listObjects( prefix );
				if ( objects.isEmpty() ){
					logger.log( LogLevel.WARN, "S3 Import Folder Adapter: The target folder prefix: {} is empty, will ignore the adapter process.", prefix );
				} else {
					objects.forEach( objectItem -> {
						String objectName = objectItem.objectName();
						logger.log( LogLevel.INFO, "S3 Import Folder Adapter: Start to download object: {} with S3 API.", objectName );
						try {
							Path saved = client.getObjectAndSave( objectName, outputPath.resolve( objectName.replace( prefix,
									Strings.BLANK ) ) );
							if ( PathUtils.isExists( saved ) ){
								logger.log( LogLevel.INFO, "S3 Import Folder Adapter: Succeeded to download object: {} with S3 API.", objectName );
							} else {
								logger.log( LogLevel.WARN, "S3 Import Folder Adapter: Failed to download object: {} with S3 API.", objectName );
							}
						} catch (MinioException | IOException e) {
							logger.log( LogLevel.ERROR, "S3 Import Folder Adapter: Download object: {} with S3 API has IOException! {}", objectName, e );
						}
					} );
				}
			} else {
				logger.log( LogLevel.WARN, "S3 Import Folder Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Import Folder Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
