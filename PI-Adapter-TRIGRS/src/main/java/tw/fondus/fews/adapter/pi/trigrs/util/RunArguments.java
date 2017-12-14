package tw.fondus.fews.adapter.pi.trigrs.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;

/**
 * Model execute-adapter arguments for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
public class RunArguments extends PiBasicArguments {
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}
}
