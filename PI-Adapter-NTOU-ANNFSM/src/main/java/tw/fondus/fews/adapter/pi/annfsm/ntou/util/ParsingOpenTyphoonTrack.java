package tw.fondus.fews.adapter.pi.annfsm.ntou.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import tw.fondus.fews.adapter.pi.annfsm.ntou.entity.Point;
import tw.fondus.fews.adapter.pi.annfsm.ntou.entity.WindDistribution;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;

/**
 * The util for parsing the JSON data of typhoon track data from CWB opendata.
 * 
 * @author Chao
 *
 */
public class ParsingOpenTyphoonTrack {
	public static final String CWB_TYPHOON_FORECAST = "cwbtyphfcst";
	public static final String TYPHOON_INFO = "typhinfo";
	public static final String TYPHOON = "typhoon";
	public static final String TYPHOON_DATA = "typhdata";
	public static final String PAST_TYPHOON_DATA = "past";
	public static final String CURRENT_TYPHOON_DATA = "curr";
	public static final String FORECAST_TYPHOON_DATA = "fcst";
	public static final String POINT = "point";
	public static final String TIME = "@time";
	public static final String LOCATION = "location";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String INTENSITY_STRUCTURE = "intensity_structure";
	public static final String INTENSITY = "intensity";
	public static final String MAX_WIND = "maxwind";
	public static final String GUST = "gust";
	public static final String CENTRAL_PRESSURE = "central_pressure";
	public static final String STRUCTURE = "structure";
	public static final String WIND_DISTRIBUTION = "wind_distribution";
	public static final String WIND_GRADE = "@wind_grade";
	public static final String DIRECTION = "dir";
	public static final String ANGLE = "angle";
	public static final String ANGLE_START = "@start";
	public static final String ANGLE_END = "@end";
	public static final String RADIUS = "radius";
	public static final String VALUE = "@value";

	/**
	 * Parsing JSON data of typhoon track data.
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<Point> parsing( String jsonString ) {
		JSONObject jsonObject = new JSONObject( jsonString );
		Object typhoonObject = jsonObject.getJSONObject( CWB_TYPHOON_FORECAST )
				.getJSONObject( TYPHOON_INFO )
				.get( "typhoon" );

		JSONObject typhoonData = new JSONObject();
		if ( isJSONArray( typhoonObject ) ) {
			JSONArray typhoonJSONObjectArray = (JSONArray) typhoonObject;
			typhoonData = typhoonJSONObjectArray.getJSONObject( 0 ).getJSONObject( TYPHOON_DATA );
		} else {
			JSONObject typhoonJSONObject = (JSONObject) typhoonObject;
			typhoonData = typhoonJSONObject.getJSONObject( TYPHOON_DATA );
		}

		Point pastPoint = getPoint( typhoonData, PAST_TYPHOON_DATA );
		Point currPoint = getPoint( typhoonData, CURRENT_TYPHOON_DATA );
		Point fcstPoint = getPoint( typhoonData, FORECAST_TYPHOON_DATA );

		List<Point> points = new ArrayList<>();
		points.add( pastPoint );
		points.add( currPoint );
		points.add( fcstPoint );

		return points;
	}

	/**
	 * Check the object is JSONArray or not.
	 * 
	 */
	private static boolean isJSONArray( Object object ) {
		if ( object instanceof JSONArray ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the typhoon track point data.
	 * 
	 * @param typhoonData
	 * @param type
	 * @return
	 */
	private static Point getPoint( JSONObject typhoonData, String type ) {
		JSONObject pointData = typhoonData.getJSONObject( type );
		Object pointObject = pointData.get( POINT );
		JSONObject pointJSONObject = new JSONObject();
		if ( isJSONArray( pointObject ) ) {
			JSONArray points = (JSONArray) pointObject;
			if ( type.equals( PAST_TYPHOON_DATA ) ) {
				pointJSONObject = points.getJSONObject( points.length() - 1 );
			} else {
				pointJSONObject = points.getJSONObject( 0 );
			}
		} else {
			pointJSONObject = (JSONObject) pointObject;
		}

		JSONObject location = pointJSONObject.getJSONObject( LOCATION );
		JSONObject intensity = pointJSONObject.getJSONObject( INTENSITY_STRUCTURE ).getJSONObject( INTENSITY );
		Optional<JSONObject> optStructure = Optional
				.ofNullable( pointJSONObject.getJSONObject( INTENSITY_STRUCTURE ).getJSONObject( STRUCTURE ) );
		BigDecimal radius = BigDecimal.ZERO;

		optStructure.ifPresent( structure -> {
			Object windDistributionObject = structure.get( WIND_DISTRIBUTION );
			JSONObject windDistributionJSONObject = new JSONObject();

			if ( isJSONArray( windDistributionObject ) ) {
				JSONArray windDistributes = (JSONArray) windDistributionObject;
				for ( int i = 0; i < windDistributes.length(); i++ ) {
					if ( windDistributes.getJSONObject( i ).getString( WIND_GRADE ).equals( "7" ) ) {
						windDistributionJSONObject = windDistributes.getJSONObject( i );
					}
				}
			} else {
				windDistributionJSONObject = (JSONObject) windDistributionObject;
			}

			JSONArray directions = windDistributionJSONObject.getJSONArray( DIRECTION );
			for ( int i = 0; i < directions.length(); i++ ) {
				JSONObject direction = directions.getJSONObject( i );
				if ( direction.getJSONObject( ANGLE ).getInt( ANGLE_START ) == 0
						&& direction.getJSONObject( ANGLE ).getInt( ANGLE_END ) == 45 ) {
					radius.add( direction.getJSONObject( RADIUS ).getBigDecimal( VALUE ) );
				} else if ( direction.getJSONObject( ANGLE ).getInt( ANGLE_START ) == 180
						&& direction.getJSONObject( ANGLE ).getInt( ANGLE_END ) == 225 ) {
					radius.add( direction.getJSONObject( RADIUS ).getBigDecimal( VALUE ) );
				}
			}
		} );

		return Point.of(
				TimeLightUtils.toDateTime( pointJSONObject.getString( TIME ), "yyyy-MM-dd'T'HH:mm:ssZ",
						TimeLightUtils.UTC8 ),
				location.getJSONObject( LATITUDE ).getBigDecimal( VALUE ),
				location.getJSONObject( LONGITUDE ).getBigDecimal( VALUE ),
				intensity.getJSONObject( MAX_WIND ).getBigDecimal( VALUE ),
				intensity.getJSONObject( GUST ).getBigDecimal( VALUE ),
				intensity.getJSONObject( CENTRAL_PRESSURE ).getBigDecimal( VALUE ),
				WindDistribution.of( "7", radius.divide( new BigDecimal( "2" ), 2, RoundingMode.HALF_UP ) ) );
	}
}
