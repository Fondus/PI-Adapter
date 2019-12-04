package tw.fondus.fews.adapter.pi.hecras.ntou.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter and post-adapter arguments for running NTOU HEC-RAS model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class ProcessArguments extends PiIOArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--tdir", "-td" }, description = "The model template directory path, relative to the current working directory." )
	private String templateDir = "Template/";
	
	@Parameter( names = { "--case", "-c" }, required = true, description = "The case name for running project of HEC-RAS model." )
	private String caseName;
}
