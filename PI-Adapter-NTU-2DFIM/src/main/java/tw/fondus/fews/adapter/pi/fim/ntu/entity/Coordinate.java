package tw.fondus.fews.adapter.pi.fim.ntu.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
 * The entity of coordinate for point data.
 * 
 * @author Chao
 *
 */
@Data
public class Coordinate {
	private BigDecimal x;
	private BigDecimal y;

	public Coordinate(BigDecimal x, BigDecimal y) {
		this.x = x;
		this.y = y;
	}
}
