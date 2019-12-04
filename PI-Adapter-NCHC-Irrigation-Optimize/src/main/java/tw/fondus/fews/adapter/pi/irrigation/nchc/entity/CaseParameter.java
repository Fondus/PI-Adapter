package tw.fondus.fews.adapter.pi.irrigation.nchc.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * The entity of case parameter.
 *
 * @author Brad Chen
 *
 */
@Data
public class CaseParameter {
	@SerializedName( "Id" )
	private String id;

	@SerializedName( "Description" )
	private String description;

	@SerializedName( "Parameters" )
	private List<Parameter> parameters;
}
