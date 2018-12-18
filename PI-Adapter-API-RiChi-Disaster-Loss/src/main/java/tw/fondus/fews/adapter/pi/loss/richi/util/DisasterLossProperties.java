package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;

/**
 * The disaster loss API properties with load initial.
 * 
 * @author Chao
 *
 */
public class DisasterLossProperties {
	/*
	 * The key name of disaster loss API.
	 */
	public static final String API_KEY_NAME = "disasterloss.api.key.name";
	
	/*
	 * The key value of disaster loss API.
	 */
	public static final String API_KEY_VALUE = "disasterloss.api.key.value";
	
	/*
	 * The URL of disaster loss API. 
	 */
	public static final String URL = "disasterloss.url.prefix";
	
	private static Map<String, String> propertiesMap;
	
	static {
		try {
			ClassLoader classLoader = DisasterLossProperties.class.getClassLoader();
			
			try (InputStream inputStream = classLoader.getResourceAsStream("disasterloss.properties")){
				Properties properties = new Properties();
				properties.load(inputStream);

				propertiesMap = ImmutableMap.of(
						API_KEY_NAME,
						properties.getProperty(API_KEY_NAME),
						API_KEY_VALUE,
						properties.getProperty(API_KEY_VALUE),
						URL,
						properties.getProperty(URL));
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
	public static Optional<String> getProperty(String key) {
		return Optional.ofNullable( propertiesMap.get(key) );
	}
	
	/**
	 * Check has key with property or not.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean containsProperty(String key) {
		return propertiesMap.containsKey(key);
	}
}
