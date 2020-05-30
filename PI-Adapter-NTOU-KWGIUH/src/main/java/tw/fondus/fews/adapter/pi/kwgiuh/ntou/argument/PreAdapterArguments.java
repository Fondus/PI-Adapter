package tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument;

import java.math.BigDecimal;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NTOU KWGIUH model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PreAdapterArguments extends PiIOArguments {
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
}
