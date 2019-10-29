package tw.fondus.fews.adapter.pi.annfsm.ntou.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import tw.fondus.fews.adapter.pi.annfsm.ntou.entity.Point;
import tw.fondus.fews.adapter.pi.annfsm.ntou.entity.WindDistribution;

/**
 * The data utils for ANNFSM adapter.
 * 
 * @author Chao
 *
 */
public class DataUtils {
	/**
	 * Interpolation typhoon track point data.
	 * 
	 * @param points
	 * @return
	 */
	public static List<Point> interpolationPoints( List<Point> points ) {
		List<Point> interpolationPoints = new ArrayList<>();
		IntStream.range( 0, points.size() - 1 ).forEach( i -> {
			long timeLag = points.get( i + 1 ).getTime().getMillis() - points.get( i ).getTime().getMillis();
			if ( timeLag > 3600000 ) {
				int hours = (int) timeLag / 3600000;
				interpolationPoints.add( points.get( i ) );
				IntStream.range( 1, hours ).forEach( hour -> {
					interpolationPoints.add( DataUtils.interpolation( points.get( i ), points.get( i + 1 ),
							new BigDecimal( hours ), new BigDecimal( hour ) ) );
				} );
			} else {
				interpolationPoints.add( points.get( i ) );
			}
		} );
		interpolationPoints.add( points.get( points.size() - 1 ) );

		return interpolationPoints;
	}

	/**
	 * Interpolation all data from point.
	 * 
	 * @param start
	 * @param end
	 * @param hours
	 * @param hour
	 * @return
	 */
	private static Point interpolation( Point start, Point end, BigDecimal hours, BigDecimal hour ) {
		/** Interpolation location data **/
		BigDecimal startLat = start.getLatitude();
		BigDecimal startLon = start.getLongitude();

		BigDecimal differentLat = end.getLatitude().subtract( startLat );
		BigDecimal differentLon = end.getLongitude().subtract( startLon );

		/** Interpolation intensity data **/
		BigDecimal startMaxWind = start.getMaxWind();
		BigDecimal startGust = start.getGust();
		BigDecimal startCentralPressure = start.getCentralPressure();

		BigDecimal differentMaxWind = end.getMaxWind().subtract( startMaxWind );
		BigDecimal differentGust = end.getGust().subtract( startGust );
		BigDecimal differentCentralPressure = end.getCentralPressure().subtract( startCentralPressure );

		/** Interpolation WindDistribution data **/
		BigDecimal startRadius = start.getWindDistribution().getRadius();

		BigDecimal differentRadius = end.getWindDistribution().getRadius().subtract( startRadius );

		return Point.of( start.getTime().plusHours( hour.intValue() ),
				startLat.add( differentLat.divide( hours, 2, 2 ).multiply( hour ) ),
				startLon.add( differentLon.divide( hours, 2, 2 ).multiply( hour ) ),
				startMaxWind.add( differentMaxWind.divide( hours, 2 ).multiply( hour ) ),
				startGust.add( differentGust.divide( hours, 2 ).multiply( hour ) ),
				startCentralPressure.add( differentCentralPressure.divide( hours, 2 ).multiply( hour ) ),
				WindDistribution.of( "7", startRadius.add( differentRadius.divide( hours, 2 ).multiply( hour ) ) ) );
	}

	/**
	 * Calculated the distance between two point.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static BigDecimal calculatedDistance( Point start, Point end ) {
		return calculatedDistance( start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude() );
	}

	/**
	 * Calculated the distance between two point.
	 * 
	 * @param startLat
	 * @param startLon
	 * @param endLat
	 * @param endLon
	 * @return
	 */
	public static BigDecimal calculatedDistance( BigDecimal startLat, BigDecimal startLon, BigDecimal endLat,
			BigDecimal endLon ) {
		return new BigDecimal( Math.abs( Math.pow(
				endLat.subtract( startLat ).pow( 2 ).add( endLon.subtract( startLon ).pow( 2 ) ).doubleValue(),
				0.5 ) ) );
	}

	/**
	 * Calculated the azimuth angle from start to end point.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static BigDecimal calculatedAngle( Point start, Point end ) {
		return calculatedAngle( start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude() );
	}

	/**
	 * Calculated the azimuth angle from start to end point.
	 * 
	 * @param startLat
	 * @param startLon
	 * @param endLat
	 * @param endLon
	 * @return
	 */
	public static BigDecimal calculatedAngle( BigDecimal startLat, BigDecimal startLon, BigDecimal endLat,
			BigDecimal endLon ) {
		double differentLon = endLon.subtract( startLon ).doubleValue();
		double differentLat = endLat.subtract( startLat ).doubleValue();

		double theta = Math.atan( differentLon / differentLat );
		if ( Double.isNaN( theta ) ) {
			theta = 0;
		}
		if ( differentLon >= 0 && differentLat >= 0 ) {
			return new BigDecimal( theta );
		} else if ( differentLon >= 0 && differentLat < 0 ) {
			return new BigDecimal( 180 - theta );
		} else if ( differentLon < 0 && differentLat < 0 ) {
			return new BigDecimal( 180 + theta );
		} else {
			return new BigDecimal( 360 - theta );
		}
	}
}
