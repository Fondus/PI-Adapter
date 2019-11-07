package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The pre-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PreArguments extends PiIOArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The executable folder, relative to the current working directory." )
	private String executablePath = "Work/";
}
