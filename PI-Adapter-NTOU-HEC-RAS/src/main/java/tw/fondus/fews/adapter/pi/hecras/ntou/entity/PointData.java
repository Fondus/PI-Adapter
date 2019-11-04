package tw.fondus.fews.adapter.pi.hecras.ntou.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * The entity of point data.
 * 
 * @author Chao
 *
 */
@Data
public class PointData {
	private BigDecimal x;
	private BigDecimal y;
	private List<Float> values;

	public PointData(BigDecimal x, BigDecimal y) {
		this.x = x;
		this.y = y;
		this.values = new ArrayList<>();
	}
	
	public void addvalue( float value ) {
		this.values.add( value );
	}
}
