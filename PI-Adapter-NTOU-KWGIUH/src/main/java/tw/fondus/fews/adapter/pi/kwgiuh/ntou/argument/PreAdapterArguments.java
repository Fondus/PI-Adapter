package tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument;

import java.math.BigDecimal;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NTOU KWGIUH model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PreAdapterArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work\\";
	
	@Parameter( names = { "--gfactor", "-gf" }, required = true, description = "The file name of geomorphic factor." )
	private String geomorphicFactor;

	@Parameter( names = { "--area", "-a" }, required = true, description = "The area of catchment(km^2)." )
	private BigDecimal area;
	
	@Parameter( names = { "--noverlandflow", "-no" }, required = true, description = "The argument of Manning N for overland flow." )
	private BigDecimal nOverlandFlow;
	
	@Parameter( names = { "--nchannel", "-nc" }, required = true, description = "The argument of Manning N for river channel." )
	private BigDecimal nChannel;
	
	@Parameter( names = { "--width", "-w" }, required = true, description = "The width of river outflow." )
	private BigDecimal width;
	
	@Parameter( names = { "--infiltration", "-inf" }, required = true, description = "The infiltration of soil." )
	private BigDecimal infiltration;
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PreAdapterArguments instance(){
		return PreAdapterArguments.builder().build();
	}
}
