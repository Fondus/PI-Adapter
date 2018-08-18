package tw.fondus.fews.adapter.pi.grid.merge.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model executable-adapter arguments for running ESRI ASCII merge model.
 * 
 * @author Brad Chen
 *
 */
public class RunArguments extends PiArguments {
	@Parameter(names = { "--tdir", "-td" }, required = true, description = "The model temp file store directory, relative to the current working directory.")
	private String tempDir;
	
	@Parameter(names = { "--edir", "-ed" }, required = true, description = "The model executable directory, relative to the current working directory.")
	private String executableDir;
	
	@Parameter(names = { "--executable", "-e" }, required = true, description = "The model executable.")
	private String executable;

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir( String tempDir ) {
		this.tempDir = tempDir;
	}

	public String getExecutableDir() {
		return executableDir;
	}

	public void setExecutableDir( String executableDir ) {
		this.executableDir = executableDir;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable( String executable ) {
		this.executable = executable;
	}
}
