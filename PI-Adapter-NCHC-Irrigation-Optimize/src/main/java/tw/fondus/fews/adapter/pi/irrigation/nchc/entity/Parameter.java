package tw.fondus.fews.adapter.pi.irrigation.nchc.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * The entity of parameter.
 *
 * @author Brad Chen
 *
 */
@Data
public class Parameter {
	@SerializedName( "Id" )
	private String id;

	@SerializedName( "Description" )
	private String description;

	@SerializedName( "Value" )
	private BigDecimal value;
}
