package tw.fondus.fews.adapter.pi.aws.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3FolderArguments;
import tw.fondus.fews.adapter.pi.aws.storage.util.PredefinePrefixUtils;
import tw.fondus.fews.adapter.pi.aws.storage.util.S3ProcessUtils;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

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
		String filePrefix = modelArguments.getFilePrefix();
		String fileSuffix = StringUtils.isNotBlank( modelArguments.getFileSuffix() ) ?
				"\\." + modelArguments.getFileSuffix() :
				Strings.EMPTY;
		String fileRegularExpression = "^" + filePrefix + ".*" + fileSuffix + "$";
		Pattern pattern = Pattern.compile( fileRegularExpression );

		MinioHighLevelClient client = MinioHighLevelClient.builder()
				.client( MinioClient.builder()
						.endpoint( host )
						.credentials( username, password )
						.build() )
				.defaultBucket( bucket )
				.build();

		try {
			S3ProcessUtils.isCreateS3BucketBefore( "S3 Export Folder Adapter", logger, client, bucket, modelArguments.isCreate() );

			logger.log( LogLevel.INFO, "S3 Export Folder Adapter: Start to upload folder: {} with S3 API.", inputPath );
			if ( client.isExistsBucket() ) {
				List<Path> paths = PathUtils.list( inputPath );
				paths.stream()
						.filter( path -> pattern.matcher( PathUtils.getName( path ) ).find() )
						.forEach( path -> {
					String prefix = PredefinePrefixUtils.createPredefinePrefix( modelArguments, path );
					String object = prefix + PathUtils.getName( path );
					S3ProcessUtils.uploadS3Object( "S3 Export Folder Adapter", logger, client, object, path );
				} );
			} else {
				logger.log( LogLevel.WARN, "S3 Export Folder Adapter: The target bucket: {} not exist, will ignore the adapter process.", bucket );
			}
			logger.log( LogLevel.INFO, "S3 Export Folder Adapter: Finished to upload folder: {} with S3 API.", inputPath );
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "S3 Export Adapter: Working with S3 API has something wrong! {}", e );
		}
	}
}
