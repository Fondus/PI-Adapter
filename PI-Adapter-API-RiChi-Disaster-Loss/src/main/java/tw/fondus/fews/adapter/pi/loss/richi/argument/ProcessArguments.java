package tw.fondus.fews.adapter.pi.loss.richi.argument;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The parameter-adapter arguments for running RiChi-DisasterLoss API model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ProcessArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--version",
			"-v" }, description = "The version of disaster loss API.(1 for old version, 2 for new)" )
	private int version = 1;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static ProcessArguments instance() {
		return ProcessArguments.builder().build();
	}
}
