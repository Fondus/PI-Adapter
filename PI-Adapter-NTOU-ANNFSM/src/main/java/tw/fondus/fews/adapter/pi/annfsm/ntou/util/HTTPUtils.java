package tw.fondus.fews.adapter.pi.annfsm.ntou.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import strman.Strman;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.http.HttpUtils;
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
		httpClient = new HttpClient();
		try {
			httpClient.setClient( HttpUtils.buildSSLClient( httpClient.getClient(),
					HttpUtils.getTrustAllSSLSocket( HttpUtils.TLS_V1 ), HttpUtils.getTrustAllManager(),
					HttpUtils.getTrustAllHostVerifier() ) );
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
				FileUtils.writeStringToFile( downloadPath.toFile(), fileString );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return downloadPath;
	}
}
