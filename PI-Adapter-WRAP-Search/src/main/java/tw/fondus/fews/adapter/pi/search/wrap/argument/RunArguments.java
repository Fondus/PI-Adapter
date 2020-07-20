package tw.fondus.fews.adapter.pi.search.wrap.argument;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model arguments.
 * 
 * @author huang shepherd
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--mdir", "-md" }, description = "The model merge directory path, relative to the current working directory." )
	private String mergeDir = "Merge/";
	@Builder.Default
	@Parameter( names = { "--floodmapdir", "-fmd" }, description = "The flood map data directory path, relative to the current working directory." )
	private String floodMapDir = "FloodedMapData/Taiwan/";

	@Parameter( names = { "--region", "-r" }, required = true, description = "The region name." )
	private String region;
	
	@Parameter( names = { "--county", "-c" }, required = true, description = "The county name." )
	private String county;
	
	@Parameter( names = { "--duration", "-d" }, required = true, description = "The rainfall duration." )
	private String duration;
	
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
