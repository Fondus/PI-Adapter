package tw.fondus.fews.adapter.pi.hecras.ntou.argument;

import java.math.BigDecimal;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter and post-adapter arguments for running NTOU HEC-RAS model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ProcessArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Builder.Default
	@Parameter( names = { "--tdir", "-td" }, description = "The model template directory path, relative to the current working directory." )
	private String templateDir = "Template/";
	
	@Parameter( names = { "--case", "-c" }, required = true, description = "The case name for running project of HEC-RAS model." )
	private String caseName;
	
	@Builder.Default
	@Parameter( names = { "--infiltration", "-inf" }, description = "The infiltration of soil." )
	private BigDecimal infiltration = BigDecimal.ZERO;
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static ProcessArguments instance(){
		return ProcessArguments.builder().build();
	}
}
