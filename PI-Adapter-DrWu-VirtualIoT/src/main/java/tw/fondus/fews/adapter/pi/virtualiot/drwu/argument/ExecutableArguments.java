package tw.fondus.fews.adapter.pi.virtualiot.drwu.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Model executable arguments for running Virtual IoT model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ExecutableArguments extends PreAdapterArguments {
	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static ExecutableArguments instance() {
		return ExecutableArguments.builder().build();
	}
}
