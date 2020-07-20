package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import com.google.gson.reflect.TypeToken;
import strman.Strman;
import tw.fondus.commons.http.HttpClient;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The parameter web API client.
 *
 * @author Brad Chen
 *
 */
public class WebAPIClient {
	private static final HttpClient httpClient;

	static {
		httpClient = HttpClient.of();
	}

	/**
	 * Get parameter api response.
	 *
	 * @param url url
	 * @param token token
	 * @return collection of case parameter
	 * @throws IOException has IO Exception
	 */
	public static List<CaseParameter> get( String url, String token ) throws IOException {
		String response = httpClient.get( url, Map.of( "Authorization", Strman.append( Strings.TOKEN_PREFIX_BEARER, token ) ) );
		return GsonMapperRuntime.DEFAULT.toBean( response, new TypeToken<List<CaseParameter>>() {}.getType() );
	}
}
