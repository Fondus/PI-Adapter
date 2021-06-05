package tw.fondus.fews.adapter.pi.argument.extend;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

/**
 * Model adapter arguments for move files inside IO folder with FEWS.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PiIOMoveArguments extends PiBasicArguments {
	@Builder.Default
	@Parameter( names = { "--source-output" }, description = "If true, use output as source and move files to input, default use input as source and move to output." )
	private boolean sourceOutput = false;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static PiIOMoveArguments instance(){
		return PiIOMoveArguments.builder().build();
	}
}
