package tw.fondus.fews.adapter.pi.annfsm.ntou.entity;

import java.math.BigDecimal;

import org.joda.time.DateTime;

import lombok.Data;

/**
 * The entity of typhoon track point data.
 * 
 * @author Chao
 *
 */
@Data
public class Point {
	private DateTime time;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private BigDecimal maxWind;
	private BigDecimal gust;
	private BigDecimal centralPressure;
	private WindDistribution windDistribution;

	public Point(DateTime time, BigDecimal latitude, BigDecimal longitude, BigDecimal maxWind, BigDecimal gust,
			BigDecimal centralPressure, WindDistribution windDistribution) {
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.maxWind = maxWind;
		this.gust = gust;
		this.centralPressure = centralPressure;
		this.windDistribution = windDistribution;
	}

	public static Point of( DateTime time, BigDecimal latitude, BigDecimal longitude, BigDecimal maxWind,
			BigDecimal gust, BigDecimal centralPressure, WindDistribution windDistribution ) {
		return new Point( time, latitude, longitude, maxWind, gust, centralPressure, windDistribution );
	}
}
