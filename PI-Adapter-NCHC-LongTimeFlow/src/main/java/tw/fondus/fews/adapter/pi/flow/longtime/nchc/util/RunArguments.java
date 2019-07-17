package tw.fondus.fews.adapter.pi.flow.longtime.nchc.util;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

/**
 * Model executable-adapter arguments for running NCHC long time flow model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiBasicArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	@Parameter( names = { "--tdir", "-td" }, description = "The template directory of model file." )
	private String templateDir = "Template/";

	@Parameter( names = { "--pname", "-pn" }, description = "The project name for backup output file name." )
	private String projectName = "ProjectName";
}
