package tw.fondus.fews.adapter.pi.annfsm.ntou.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;

import okhttp3.logging.HttpLoggingInterceptor;
import strman.Strman;
import tw.fondus.commons.http.HttpClient;
import tw.fondus.commons.http.util.HttpUtils;
import tw.fondus.commons.http.util.HttpsProtocol;
import tw.fondus.commons.http.util.OkHttpUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.optional.OptionalUtils;

/**
 * The HTTP utils of download file with CWB open-data.
 * 
 * @author Chao
 *
 */
public class HTTPUtils {
	private static HttpClient httpClient;

	static {
		try {
			httpClient = HttpClient.of( OkHttpUtils.builder()
					.addInterceptor( OkHttpUtils.loggingInterceptor( HttpLoggingInterceptor.Level.BASIC ) )
					.sslSocketFactory(
							HttpUtils.getTrustAllSSLSocket( HttpsProtocol.TLS_1_2 ),
							HttpUtils.getTrustAllManager() )
					.hostnameVerifier( HttpUtils.getTrustAllHostVerifier() )
					.build());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get file from open-data site and write to specified target.
	 * 
	 * @param dataId
	 * @param token
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public static Path getFile( String dataId, String token, String filePath ) {
		Optional<String> optURL = OpenDataProperties.getProperty( OpenDataProperties.URL );
		Optional<String> optUrlAuthor = OpenDataProperties.getProperty( OpenDataProperties.PARAMETER_AUTHOR );
		Optional<String> optUrlFormat = OpenDataProperties.getProperty( OpenDataProperties.PARAMETER_FORMAT );

		Path downloadPath = Paths.get( filePath );
		if ( OptionalUtils.allMatch( optURL, optUrlAuthor, optUrlFormat ) ) {
			try {
				String url = Strman.append( optURL.get(), dataId, optUrlAuthor.get(), token, optUrlFormat.get() );

				String fileString = httpClient.get( url );
				PathWriter.write( downloadPath, fileString );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return downloadPath;
	}
}
