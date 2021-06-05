package tw.fondus.fews.adapter.pi.nc.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The adapter arguments for merged the grid of NetCDF.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class MergeArguments extends PiIOArguments {
	@Parameter( names = { "-c", "--count" }, required = true, description = "The count of file size, if exceed will cause exception." )
	private int count;

	@Parameter( names = { "-tr", "--time-range" }, required = true, description = "The time range will used to read from file, if file time lower than index will cause exception." )
	private int timeRange;

	@Builder.Default
	@Parameter( names = { "--delete-inputs" }, description = "Delete inputs after process." )
	private boolean deleteInputs = false;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.1.0
	 */
	public static MergeArguments instance(){
		return MergeArguments.builder().build();
	}
}
