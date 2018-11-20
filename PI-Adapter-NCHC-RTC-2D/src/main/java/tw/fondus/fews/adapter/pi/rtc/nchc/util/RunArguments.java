package tw.fondus.fews.adapter.pi.rtc.nchc.util;

import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * Model executable-adapter arguments for running NCHC RTC model.
 * 
 * @author Chao
 *
 */
public class RunArguments extends PreArguments {
	@Parameter( names = { "--edir",
			"-ed" }, required = true, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir;

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;

	@Parameter( names = { "--tdir",
			"-td" }, description = "The template directory of temp file of model input or output." )
	protected String tempDir = "Template/";

	@Parameter( names = { "--pname",
			"-pn" }, description = "The project name for backup output file name." )
	protected String projectName = "ProjectName";

	public String getExecutableDir() {
		return executableDir;
	}

	public void setExecutableDir( String executableDir ) {
		this.executableDir = executableDir;
	}

	public List<String> getExecutable() {
		return executable;
	}

	public void setExecutable( List<String> executable ) {
		this.executable = executable;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir( String tempDir ) {
		this.tempDir = tempDir;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName( String projectName ) {
		this.projectName = projectName;
	}
	
}
