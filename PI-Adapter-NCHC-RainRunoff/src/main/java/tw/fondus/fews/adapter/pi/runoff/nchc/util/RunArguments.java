package tw.fondus.fews.adapter.pi.runoff.nchc.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model executable-adapter arguments for running NCHC RR model.
 * 
 * @author Brad Chen
 *
 */
public class RunArguments extends PiArguments {
	@Parameter(names = { "--parametersPath", "-pp" }, required = true, description = "The model parameters directory, relative to the current working directory.")
	private String parametersPath;
	
	@Parameter(names = { "--executablePath", "-ep" }, required = true, description = "The model executable directory path, relative to the current working directory.")
	private String executablePath;
	
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executble;

	public String getParametersPath() {
		return parametersPath;
	}

	public void setParametersPath(String parametersPath) {
		this.parametersPath = parametersPath;
	}

	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	public String getExecutble() {
		return executble;
	}

	public void setExecutble(String executble) {
		this.executble = executble;
	}
}
