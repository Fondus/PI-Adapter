package tw.fondus.fews.adapter.pi.runoff.nchc.util;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running NCHC RR model.
 * 
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiIOArguments {
	@Parameter(names = { "--pdir", "-pd" }, required = true, description = "The model parameters directory, relative to the current working directory.")
	private String parametersPath;
	
	@Parameter(names = { "--edir", "-ed" }, required = true, description = "The model executable directory path, relative to the current working directory.")
	private String executablePath;
	
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;
}
