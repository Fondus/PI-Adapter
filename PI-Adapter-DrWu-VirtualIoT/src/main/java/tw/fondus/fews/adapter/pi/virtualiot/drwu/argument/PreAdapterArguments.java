package tw.fondus.fews.adapter.pi.virtualiot.drwu.argument;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Model pre-adapter arguments for running Virtual IoT model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PreAdapterArguments extends PostAdapterArguments {
	@Builder.Default
	@Parameter( names = { "--tdir",
			"-td" }, description = "The model template directory path, relative to the current working directory." )
	private String templateDir = "Template";

	@Parameter( names = { "--basin ", "-ba" }, required = true, description = "The basin name for running model." )
	private String basin;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static PreAdapterArguments instance() {
		return PreAdapterArguments.builder().build();
	}
}
