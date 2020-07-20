package tw.fondus.fews.adapter.pi.nc.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The adapter arguments for restructure the grid type of NetCDF.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RestructureArguments extends PiIOArguments {
	@Parameter( names = { "-vn" }, required = true, description = "The name of input NetCDF variable name." )
	private String variableName;

	@Parameter( names = { "-xn" }, required = true, description = "The name of input NetCDF X variable." )
	private String xName;

	@Parameter( names = { "-yn" }, required = true, description = "The name of input NetCDF Y variable." )
	private String yName;

	@Parameter( names = { "-tn" }, required = true, description = "The name of input NetCDF Time variable." )
	private String tName;

	@Builder.Default
	@Parameter( names = { "-xo" }, description = "The order of input NetCDF X dimension." )
	private int xOrder = 2;

	@Builder.Default
	@Parameter( names = { "-yo" }, description = "The order of input NetCDF Y dimension." )
	private int yOrder = 1;

	@Builder.Default
	@Parameter( names = { "-to" }, description = "The order of input NetCDF Time dimension." )
	private int tOrder = 0;

	@Builder.Default
	@Parameter( names = { "-tzFlag" }, description = "The flag use to append time zone to time unit." )
	private boolean isTimeZoneFlag = false;

	@Builder.Default
	@Parameter( names = { "-tz" }, description = "The time zone will be append to time unit, if flag set be true, default is +0000." )
	private String timeZone = "+0000";

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static RestructureArguments instance(){
		return RestructureArguments.builder().build();
	}
}
