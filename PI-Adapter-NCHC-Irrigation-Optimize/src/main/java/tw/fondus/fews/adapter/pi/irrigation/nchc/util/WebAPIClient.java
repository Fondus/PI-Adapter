package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import com.google.gson.reflect.TypeToken;
import strman.Strman;
import tw.fondus.commons.json.util.JSONUtils;
import tw.fondus.commons.json.util.token.JWTUtils;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;

import java.io.IOException;
import java.util.List;

/**
 * The parameter web API client.
 *
 * @author Brad Chen
 *
 */
public class WebAPIClient {
	private static HttpClient httpClient;

	static {
		httpClient = new HttpClient();
	}

	/**
	 * Get parameter api response.
	 *
	 * @param url
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public static List<CaseParameter> get( String url, String token ) throws IOException {
		String response = httpClient.get( url, "Authorization", Strman.append( JWTUtils.TOKEN_PREFIX, token ) );
		return JSONUtils.fromJSON( response, new TypeToken<List<CaseParameter>>() {}.getType() );
	}
}
