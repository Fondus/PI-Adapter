package tw.fondus.fews.adapter.pi.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.BasicNoIOArguments;

/**
 * Standard arguments use for parse string arguments.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PiCommandArguments extends BasicNoIOArguments {
	@Parameter( names = { "--command", "-c" }, required = true, description = "The arguments, example like 'cd src/test/resources'." )
	private String command;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static PiCommandArguments instance(){
		return PiCommandArguments.builder().build();
	}
}
