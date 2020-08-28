package tw.fondus.fews.adapter.pi.rainfall.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The index arguments of RainfallAccumulateAdapter.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class IndexArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--skip", "-s" }, description = "The skip index number." )
	private int skip = 0;

	@Builder.Default
	@Parameter( names = { "--start", "-si" }, required = true, description = "The start index number." )
	private int start;

	@Builder.Default
	@Parameter( names = { "--end", "-ei" }, required = true, description = "The end index number." )
	private int end;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static IndexArguments instance(){
		return IndexArguments.builder().build();
	}
}
