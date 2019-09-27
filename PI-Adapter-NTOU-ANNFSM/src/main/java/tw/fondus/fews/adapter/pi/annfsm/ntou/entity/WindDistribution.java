package tw.fondus.fews.adapter.pi.annfsm.ntou.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
 * The entity of typhoon wind distribution.
 * 
 * @author Chao
 *
 */
@Data
public class WindDistribution {
	private String windGrade;
	private BigDecimal radius;

	public WindDistribution(String windGrade, BigDecimal radius) {
		this.windGrade = windGrade;
		this.radius = radius;
	}

	public static WindDistribution of( String windGrade, BigDecimal radius ) {
		return new WindDistribution( windGrade, radius );
	}
}
