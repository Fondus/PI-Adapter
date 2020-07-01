package tw.fondus.fews.adapter.pi.hecras.ntou.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import tw.fondus.commons.util.string.Strings;

/***
 * The properties for mapping variable name and replacing key string.
 * 
 * @author Chao
 *
 */
public class MappingProperties {
	public static final String SIMULATE_DATE = "key.time.simulation";
	public static final String TIME_STEPS = "key.time.steps";
	public static final String UPSTREAM_FLOW = "key.upstream.flow";
	public static final String UPSTREAM_FLOW_SIZE = "key.upstream.flow.size";
	public static final String UPSTREAM_TIDE = "key.upstream.tide";
	public static final String UPSTREAM_TIDE_SIZE = "key.upstream.tide.size";
	public static final String RAINFALL_LEFT = "key.rainfall.left";
	public static final String RAINFALL_LEFT_SIZE = "key.rainfall.left.size";
	public static final String RAINFALL_RIGHT = "key.rainfall.right";
	public static final String RAINFALL_RIGHT_SIZE = "key.rainfall.right.size";
	
	public static final String HDF5_COORDINATE_RIGHT = "hdf5.key.coordinate.right";
	public static final String HDF5_COORDINATE_LEFT = "hdf5.key.coordinate.left";
	public static final String HDF5_DEPTH_RIGHT = "hdf5.key.depth.right";
	public static final String HDF5_DEPTH_LEFT = "hdf5.key.depth.left";
	public static final String HDF5_TIME = "hdf5.key.time";
	public static final String HDF5_TIME_ATTRIBUTE = "hdf5.key.time.attribute";
	public static final String HDF5_FLOW_VALUE="hdf5.key.flow.value";
	public static final String HDF5_FLOW_COORDINATE="hdf5.key.flow.coordinate";

	private static Map<String, String> propertiesMap;

	static {
		ClassLoader classLoader = MappingProperties.class.getClassLoader();

		try (InputStream inputStream = classLoader.getResourceAsStream( "mapping.properties" ) ) {
			Properties properties = new Properties();
			properties.load( inputStream );

			propertiesMap = new HashMap<>();
			propertiesMap.put( SIMULATE_DATE, properties.getProperty( SIMULATE_DATE ) );
			propertiesMap.put( TIME_STEPS, properties.getProperty( TIME_STEPS ) );
			propertiesMap.put( UPSTREAM_FLOW, properties.getProperty( UPSTREAM_FLOW ) );
			propertiesMap.put( UPSTREAM_FLOW_SIZE, properties.getProperty( UPSTREAM_FLOW_SIZE ) );
			propertiesMap.put( UPSTREAM_TIDE, properties.getProperty( UPSTREAM_TIDE ) );
			propertiesMap.put( UPSTREAM_TIDE_SIZE, properties.getProperty( UPSTREAM_TIDE_SIZE ) );
			propertiesMap.put( RAINFALL_LEFT, properties.getProperty( RAINFALL_LEFT ) );
			propertiesMap.put( RAINFALL_LEFT_SIZE, properties.getProperty( RAINFALL_LEFT_SIZE ) );
			propertiesMap.put( RAINFALL_RIGHT, properties.getProperty( RAINFALL_RIGHT ) );
			propertiesMap.put( RAINFALL_RIGHT_SIZE, properties.getProperty( RAINFALL_RIGHT_SIZE ) );
			
			propertiesMap.put( HDF5_COORDINATE_RIGHT, properties.getProperty( HDF5_COORDINATE_RIGHT ) );
			propertiesMap.put( HDF5_COORDINATE_LEFT, properties.getProperty( HDF5_COORDINATE_LEFT ) );
			propertiesMap.put( HDF5_DEPTH_RIGHT, properties.getProperty( HDF5_DEPTH_RIGHT ) );
			propertiesMap.put( HDF5_DEPTH_LEFT, properties.getProperty( HDF5_DEPTH_LEFT ) );
			propertiesMap.put( HDF5_TIME, properties.getProperty( HDF5_TIME ) );
			propertiesMap.put( HDF5_TIME_ATTRIBUTE, properties.getProperty( HDF5_TIME_ATTRIBUTE ) );
			propertiesMap.put( HDF5_FLOW_VALUE, properties.getProperty( HDF5_FLOW_VALUE ) );
			propertiesMap.put( HDF5_FLOW_COORDINATE, properties.getProperty( HDF5_FLOW_COORDINATE ) );
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
	public static String getProperty( String key ) {
		return propertiesMap.containsKey( key ) ? propertiesMap.get( key ) : Strings.BLANK;
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
