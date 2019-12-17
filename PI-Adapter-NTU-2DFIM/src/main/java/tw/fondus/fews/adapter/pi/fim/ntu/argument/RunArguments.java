package tw.fondus.fews.adapter.pi.fim.ntu.argument;

import java.math.BigDecimal;
import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running NTU 2DFIM model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiIOArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	@Parameter( names = { "--tdir", "-td" }, description = "The model executable directory path, relative to the current working directory." )
	private String templateDir = "Template/";

	@Parameter( names = { "--forecast", "-f" }, required = true, description = "The time steps of forecasting for running model." )
	private BigDecimal forecast;
}
