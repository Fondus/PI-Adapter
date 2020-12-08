package tw.fondus.fews.adapter.pi.virtualiot.drwu.argument;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model post-adapter arguments for running Virtual IoT model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PostAdapterArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--edir",
			"-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work";

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static PostAdapterArguments instance() {
		return PostAdapterArguments.builder().build();
	}
}
