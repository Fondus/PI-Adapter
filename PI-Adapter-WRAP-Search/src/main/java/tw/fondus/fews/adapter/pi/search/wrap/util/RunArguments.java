package tw.fondus.fews.adapter.pi.search.wrap.util;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model arguments.
 * 
 * @author huang shepherd
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiIOArguments {

	@Parameter( names = { "--mdir", "-md" }, description = "The model merge directory path, relative to the current working directory." )
	private String mergeDir = "Merge/";

	@Parameter( names = { "--floodmapdir", "-fmd" }, description = "The flood map data directory path, relative to the current working directory." )
	private String floodMapDir = "FloodedMapData/Taiwan/";

	@Parameter( names = { "--region", "-r" }, required = true, description = "The region name." )
	private String region;
	
	@Parameter( names = { "--county", "-c" }, required = true, description = "The county name." )
	private String county;
	
	@Parameter( names = { "--duration", "-d" }, required = true, description = "The rainfall duration." )
	private String duration;
}
