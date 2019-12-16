package tw.fondus.fews.adapter.pi.fim.ntu.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
 * The entity of point data.
 * 
 * @author Chao
 *
 */
@Data
public class PointData {
	private Coordinate coordinate;
	private BigDecimal value;

	public PointData(BigDecimal x, BigDecimal y, BigDecimal value) {
		this.coordinate = new Coordinate( x, y );
		this.value = value;
	}
}
