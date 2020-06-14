package tw.fondus.fews.adapter.pi.argument.extend;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The expand argument it used with AdjustTimeContentAdapter.
 *
 * @author Brad Chen
 * @since 3.0.0
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class AdjustTimeArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--mode", "-m" }, description = "The adjust time mode, 0 is adjust by base.xml, 1 is adjust by time zero, default is 0." )
	private int mode = 0;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static AdjustTimeArguments instance(){
		return AdjustTimeArguments.builder().build();
	}
}
