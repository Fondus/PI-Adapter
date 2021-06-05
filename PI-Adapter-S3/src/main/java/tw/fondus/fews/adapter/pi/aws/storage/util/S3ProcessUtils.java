package tw.fondus.fews.adapter.pi.aws.storage.util;

import io.minio.errors.MinioException;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The tool of S3 process.
 *
 * @author Brad Chen
 *
 */
public class S3ProcessUtils {
	private S3ProcessUtils() {}

	/**
	 * Process used check is need create bucket before process
	 *
	 * @param adapterName name of adapter used to logging
	 * @param logger logger
	 * @param client s3 client
	 * @param bucket s3 bucket
	 * @param isCreate is create
	 */
	public static void isCreateS3BucketBefore( String adapterName, PiDiagnosticsLogger logger,
			MinioHighLevelClient client, String bucket, boolean isCreate ){
		try {
			if ( client.isNotExistsBucket() && isCreate ){
				logger.log( LogLevel.INFO, "{}: The target bucket: {} created by adapter.", adapterName, bucket );
				client.createBucket();
			}
		} catch (MinioException e) {
			logger.log( LogLevel.ERROR, "{}: Check or create bucket with S3 API has something wrong! {}", adapterName, e );
		}
	}

	/**
	 * Upload object process of S3.
	 *
	 * @param adapterName name of adapter used to logging
	 * @param logger logger
	 * @param client s3 client
	 * @param object s3 object
	 * @param input resource path
	 */
	public static void uploadS3Object( String adapterName, PiDiagnosticsLogger logger,
			MinioHighLevelClient client, String object, Path input ){
		try {
			boolean state = client.uploadObject( object, input );
			if ( state ){
				logger.log( LogLevel.INFO, "{}: Succeeded to upload object: {} with S3 API.", adapterName, object );
			} else {
				logger.log( LogLevel.WARN, "{}: Failed to upload object: {} with S3 API.", adapterName, object );
			}
		} catch (MinioException | IOException e) {
			logger.log( LogLevel.ERROR, "{}: Upload object: {} with S3 API has IOException! {}", adapterName, object, e );
		}
	}

	/**
	 * Delete object process of S3.
	 *
	 * @param adapterName name of adapter used to logging
	 * @param logger logger
	 * @param client s3 client
	 * @param object s3 object
	 */
	public static void deleteS3Object( String adapterName, PiDiagnosticsLogger logger,
			MinioHighLevelClient client, String object ){
		if ( client.isNotExistsObject( object ) ){
			logger.log( LogLevel.WARN, "{}: The target object: {} not exist, will skip process.", adapterName, object );
		} else {
			try {
				boolean state = client.removeObject( object );
				if ( state ) {
					logger.log( LogLevel.INFO, "{}: Successful to delete target object: {}.", adapterName, object );
				} else {
					logger.log( LogLevel.WARN, "{}: Failed to delete target object: {}.", adapterName, object );
				}
			} catch (MinioException e) {
				logger.log( LogLevel.ERROR, "{}: Delete object: {} with S3 API has something wrong! {}", adapterName,
						object, e );
			}
		}
	}

	/**
	 * Download object process of S3.
	 *
	 * @param adapterName name of adapter used to logging
	 * @param logger logger
	 * @param client s3 client
	 * @param object s3 object
	 * @param output output resource path
	 */
	public static void downloadS3Object( String adapterName, PiDiagnosticsLogger logger,
			MinioHighLevelClient client, String object, Path output ){
		logger.log( LogLevel.INFO, "{}: Download object: {} with S3 API.", adapterName, object );
		try {
			Path saved = client.getObjectAndSave( object, output );
			if ( PathUtils.isExists( saved ) ){
				logger.log( LogLevel.INFO, "{}: Succeeded to download object: {} with S3 API.", adapterName, object );
			} else {
				logger.log( LogLevel.WARN, "{}: Failed to download object: {} with S3 API.", adapterName, object );
			}
		} catch (MinioException | IOException e) {
			logger.log( LogLevel.ERROR, "{}: Download object: {} with S3 API has something wrong! {}", adapterName,
					object, e );
		}
	}
}
