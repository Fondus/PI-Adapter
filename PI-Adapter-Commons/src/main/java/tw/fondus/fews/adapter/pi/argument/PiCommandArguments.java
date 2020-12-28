package tw.fondus.fews.adapter.pi.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
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

	@Builder.Default
	@Parameter( names = { "--log", "-l" }, description = "The diagnostics log file name." )
	private String diagnostics = "Diagnostics.xml";

	@Builder.Default
	@Parameter( names = { "--ldir", "-ld" }, description = "The diagnostics folder, relative to the current working directory." )
	private String logPath = "Diagnostics/";

	@Builder.Default
	@Parameter( names = { "--command-log", "-cl" }, description = "Write command log into diagnostics or not, default is false." )
	private boolean writeCommandLogging = false;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static PiCommandArguments instance(){
		return PiCommandArguments.builder().build();
	}
}
