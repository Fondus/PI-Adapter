package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.http.HttpUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;

public class DisasterLossUtils {
	public static final String URL = "localhost";
	public static final String EVENT = "event";
	public static final String KEY = "file";
	
	public static long getDataDateLong( Path mapStacksPath ) throws Exception {
		MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath.toFile(), MapStacks.class );

		return TimeUtils.toDate(
				Strman.append( mapStacks.getMapStacks().get( 0 ).getStartDate().getDate(), StringUtils.SPACE_WHITE,
						mapStacks.getMapStacks().get( 0 ).getStartDate().getTime() ),
				TimeUtils.YMDHMS, TimeUtils.GMT0 ).getTime();
	}
	
	public static OkHttpClient buildClientWithTimeout( long timeout ){
		return new OkHttpClient().newBuilder()
				.connectTimeout( timeout, TimeUnit.SECONDS )
				.readTimeout( timeout, TimeUnit.SECONDS )
				.writeTimeout( timeout, TimeUnit.SECONDS )
				.build();
	}

	public static String postDisasterLossAPI( HttpClient client, Path ascPath ) throws IOException{
		return postDisasterLossAPI( client, ascPath, EVENT );
	}
	
	public static String postDisasterLossAPI( HttpClient client, Path ascPath, String event ) throws IOException{
		RequestBody requestBody = HttpUtils.createFormDataBody( KEY, ascPath.toFile().getName(), ascPath.toFile() );
		return client.postForm( Strman.append( URL, event ), requestBody );
	}
}
