package tw.fondus.fews.adapter.pi.flow.longtime.nchc.util;

import java.util.List;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model executable-adapter arguments for running NCHC long time flow model.
 * 
 * @author Chao
 *
 */
public class RunArguments extends PiArguments {
	@Parameter( names = { "--edir",
			"-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	@Parameter( names = { "--tdir",
			"-td" }, description = "The template directory of model file." )
	protected String templateDir = "Template/";

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

	public String getTemplateDir() {
		return templateDir;
	}

	public void setTemplateDir( String templateDir ) {
		this.templateDir = templateDir;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName( String projectName ) {
		this.projectName = projectName;
	}
	
}
