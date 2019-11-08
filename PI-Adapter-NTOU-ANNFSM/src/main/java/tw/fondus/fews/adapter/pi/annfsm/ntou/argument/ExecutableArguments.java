package tw.fondus.fews.adapter.pi.annfsm.ntou.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable arguments for running NTOU ANNFSM model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class ExecutableArguments extends PiIOArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
}