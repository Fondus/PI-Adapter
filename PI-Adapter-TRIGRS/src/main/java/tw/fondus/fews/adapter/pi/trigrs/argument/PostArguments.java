package tw.fondus.fews.adapter.pi.trigrs.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model post-adapter arguments for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PostArguments extends PiIOArguments {
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The Model time duration of end period.")
	private int after;
}
