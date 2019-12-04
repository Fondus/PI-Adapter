package tw.fondus.fews.adapter.pi.annfsm.ntou.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;

/**
 * The open data properties with load initial.
 * 
 * @author Chao
 *
 */
public class OpenDataProperties {
	/**
	 * The token.
	 */
	public static final String TOKEN = "opendata.user.token";

	/**
	 * The base URL for the open-data.
	 */
	public static final String URL = "opendata.url.prefix";

	/**
	 * The URL get parameters for the author token.
	 */
	public static final String PARAMETER_AUTHOR = "opendata.url.author";

	/**
	 * The URL get parameters for the data format.
	 */
	public static final String PARAMETER_FORMAT = "opendata.url.format";

	/**
	 * The source id from opendata.
	 */
	public static final String SOURCE_ID = "opendata.source.id";

	private static Map<String, String> propertiesMap;

	static {
		try {
			ClassLoader classLoader = OpenDataProperties.class.getClassLoader();

			try (InputStream inputStream = classLoader.getResourceAsStream( "opendata.properties" ) ) {
				Properties properties = new Properties();
				properties.load( inputStream );

				propertiesMap = ImmutableMap.of( 
						TOKEN, properties.getProperty( TOKEN ), 
						URL, properties.getProperty( URL ), 
						PARAMETER_AUTHOR, properties.getProperty( PARAMETER_AUTHOR ),
						PARAMETER_FORMAT, properties.getProperty( PARAMETER_FORMAT ),
						SOURCE_ID, properties.getProperty( SOURCE_ID ));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get property with key.
	 * 
	 * @param key
	 * @return
	 */
	public static Optional<String> getProperty( String key ) {
		return Optional.ofNullable( propertiesMap.get( key ) );
	}

	/**
	 * Check has key with property or not.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean containsProperty( String key ) {
		return propertiesMap.containsKey( key );
	}
}
