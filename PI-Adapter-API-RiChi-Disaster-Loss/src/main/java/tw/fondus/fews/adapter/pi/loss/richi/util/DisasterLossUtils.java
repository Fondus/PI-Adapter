package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import nl.wldelft.util.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.http.HttpUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;

/**
 * The utils of Disaster Loss Adapter.
 * 
 * @author Chao
 *
 */
public class DisasterLossUtils {
	public static final String EVENT = "event";
	public static final String KEY = "file";

	public static final String DAY = "day";
	public static final String HOUR = "hour";
	public static final String MINUTE = "minute";

	public static final int DAY_MILLISECOND = 86400000;
	public static final int HOUR_MILLISECOND = 3600000;
	public static final int MINUTE_MILLISECOND = 60000;

	/**
	 * Get ASC file absolute path.
	 * 
	 * @param inputPath
	 * @param mapStack
	 * @param step
	 * @return
	 */
	public static String getASCAbsolutePath( Path inputPath, MapStack mapStack, int step){
		String fileName = mapStack.getFile().getPattern().getFile();
		return Strman.append( inputPath.toString(), StringUtils.PATH, fileName.substring( 0, fileName.indexOf( "?" ) ), getFileExtWithNumber( step ));
	}
	
	/**
	 * Get file name extension with time step.
	 * EX: Test????.??? -> Test0000.001
	 * 
	 * @param step
	 * @return
	 */
	private static String getFileExtWithNumber( int step ){
		String result = String.format( "%07d", step );
		return Strman.append( result.substring( 0, 4 ), StringUtils.DOT,result.substring( 4, result.length() ) );
	}
	
	/**
	 * Calculate time steps with map stack.
	 * 
	 * @param mapStack
	 * @return
	 * @throws Exception
	 */
	public static int calculateTimeSteps( MapStack mapStack ) throws ParseException {
		long start = TimeUtils.toDate( Strman.append( mapStack.getStartDate().getDate(), StringUtils.SPACE_WHITE,
				mapStack.getStartDate().getTime() ), TimeUtils.YMDHMS, TimeUtils.GMT0 ).getTime();
		long end = TimeUtils.toDate( Strman.append( mapStack.getEndDate().getDate(), StringUtils.SPACE_WHITE,
				mapStack.getEndDate().getTime() ), TimeUtils.YMDHMS, TimeUtils.GMT0 ).getTime();

		if ( mapStack.getTimeStep().getUnit().equals( DAY ) ) {
			return (int) ((end - start) / (DAY_MILLISECOND));
		} else if ( mapStack.getTimeStep().getUnit().equals( HOUR ) ) {
			return (int) ((end - start) / (HOUR_MILLISECOND));
		} else {
			return (int) ((end - start) / (MINUTE_MILLISECOND));
		}
	}

	/**
	 * Rename file extension when if not a asc file extension.
	 * 
	 * @param path
	 * @param inputPath
	 * @return
	 * @throws IOException
	 */
	public static Path renameToASC( Path path, Path inputPath ) throws IOException {
		File file = path.toFile();
		if ( !FileUtils.getFileExt( file ).equals( FileType.ASC.getType() ) ) {
			String fileName = FileUtils.getNameWithoutExt( file );

			Path newPath = Paths.get( Strman.append( inputPath.toString(), StringUtils.PATH, fileName, FileType.ASC.getExtension() ) );
			FileUtils.move( file.getPath(), newPath.toFile().getPath() );

			return newPath;
		} else {
			return path;
		}
	}
	
	/**
	 * Get data date long value with map stack.
	 * 
	 * @param mapStacksPath
	 * @return
	 * @throws ParseException 
	 * @throws Exception
	 */
	public static long getDataDateLong( MapStack mapStack, int step ) throws ParseException {
		long start = TimeUtils.toDate( Strman.append( mapStack.getStartDate().getDate(), StringUtils.SPACE_WHITE,
				mapStack.getStartDate().getTime() ), TimeUtils.YMDHMS, TimeUtils.GMT0 ).getTime();

		if ( mapStack.getTimeStep().getUnit().equals( DAY ) ) {
			return (start + (step * DAY_MILLISECOND));
		} else if ( mapStack.getTimeStep().getUnit().equals( HOUR ) ) {
			return (start + (step * HOUR_MILLISECOND));
		} else {
			return (start + (step * MINUTE_MILLISECOND));
		}
	}

	/**
	 * Build OkHttpClient with timeout setting.
	 * 
	 * @param timeout
	 * @return
	 */
	public static OkHttpClient buildClientWithTimeout( long timeout ) {
		return new OkHttpClient().newBuilder()
				.connectTimeout( timeout, TimeUnit.SECONDS )
				.readTimeout( timeout, TimeUnit.SECONDS )
				.writeTimeout( timeout, TimeUnit.SECONDS )
				.build();
	}

	/**
	 * Post API of RiChi Disaster Loss.
	 * 
	 * @param client
	 * @param ascPath
	 * @return
	 * @throws IOException
	 */
	public static String postDisasterLossAPI( HttpClient client, Path ascPath ) throws IOException {
		Optional<String> optURL = DisasterLossProperties.getProperty( DisasterLossProperties.URL );
		Optional<String> optKeyName = DisasterLossProperties.getProperty( DisasterLossProperties.API_KEY_NAME );
		Optional<String> optKeyValue = DisasterLossProperties.getProperty( DisasterLossProperties.API_KEY_VALUE );
		
		if( OptionalUtils.allMatch( optURL, optKeyName, optKeyValue ) ){
			RequestBody requestBody = HttpUtils.createFormDataBody( KEY, ascPath.toFile().getName(), ascPath.toFile() );
			return client.postForm( Strman.append( optURL.get(), EVENT ), requestBody, optKeyName.get(), optKeyValue.get() );
		}else {
			throw new IOException( "There are empty value of disaster loss properties" );
		}
	}
}
