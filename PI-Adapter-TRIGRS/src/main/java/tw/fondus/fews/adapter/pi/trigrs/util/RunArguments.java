package tw.fondus.fews.adapter.pi.trigrs.util;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

/**
 * Model execute-adapter arguments for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiBasicArguments {
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;
}
