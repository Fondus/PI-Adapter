package tw.fondus.fews.adapter.pi.nc.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The adapter arguments for rename the NetCDF by time value.
 *
 * @author Brad Chen
 * @since 3.2.5
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RenameByTimeValueArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "-tf", "--time-format" }, description = "The rename file time format, default is 'yyyyMMddHHmm'." )
	private String timeFormat = "yyyyMMddHHmm";

	@Parameter( names = { "-ti", "--time-index" }, required = true, description = "The time index will be use, if index greater than file contains will throw exception." )
	private int timeIndex;

	@Builder.Default
	@Parameter( names = { "--gmt8" }, description = "The time zone flat to use GMT8, default is UTC0." )
	private boolean isGMT8 = false;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static RenameByTimeValueArguments instance(){
		return RenameByTimeValueArguments.builder().build();
	}
}
