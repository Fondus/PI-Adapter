package tw.fondus.fews.adapter.pi.trigrs.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model post-adapter arguments for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PostArguments extends PiIOArguments {
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The Model time duration of end period.")
	private int after;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PostArguments instance(){
		return PostArguments.builder().build();
	}
}
