package tw.fondus.fews.adapter.pi.fim.ntu.argument;

import java.math.BigDecimal;
import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running NTU 2DFIM model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	@Builder.Default
	@Parameter( names = { "--tdir", "-td" }, description = "The model executable directory path, relative to the current working directory." )
	private String templateDir = "Template/";

	@Parameter( names = { "--forecast", "-f" }, required = true, description = "The time steps of forecasting for running model." )
	private BigDecimal forecast;
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static RunArguments instance(){
		return RunArguments.builder().build();
	}
}
