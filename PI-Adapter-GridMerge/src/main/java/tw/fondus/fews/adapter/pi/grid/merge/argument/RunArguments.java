package tw.fondus.fews.adapter.pi.grid.merge.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running ESRI ASCII merge model.
 * 
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PiIOArguments {
	@Parameter(names = { "--tdir", "-td" }, required = true, description = "The model temp file store directory, relative to the current working directory.")
	private String tempDir;
	
	@Parameter(names = { "--edir", "-ed" }, required = true, description = "The model executable directory, relative to the current working directory.")
	private String executableDir;
	
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static RunArguments instance(){
		return RunArguments.builder().build();
	}
}
