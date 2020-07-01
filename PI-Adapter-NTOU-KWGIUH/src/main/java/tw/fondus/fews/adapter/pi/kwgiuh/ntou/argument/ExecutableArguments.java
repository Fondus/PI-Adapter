package tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter and post-adapter arguments for running NTOU KWGIUH model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ExecutableArguments extends PiIOArguments{
	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static ExecutableArguments instance(){
		return ExecutableArguments.builder().build();
	}
}
