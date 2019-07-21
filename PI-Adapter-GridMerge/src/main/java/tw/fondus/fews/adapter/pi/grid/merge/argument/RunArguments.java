package tw.fondus.fews.adapter.pi.grid.merge.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running ESRI ASCII merge model.
 * 
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiIOArguments {
	@Parameter(names = { "--tdir", "-td" }, required = true, description = "The model temp file store directory, relative to the current working directory.")
	private String tempDir;
	
	@Parameter(names = { "--edir", "-ed" }, required = true, description = "The model executable directory, relative to the current working directory.")
	private String executableDir;
	
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;
}
