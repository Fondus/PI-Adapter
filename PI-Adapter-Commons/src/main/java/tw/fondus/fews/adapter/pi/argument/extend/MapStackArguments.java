package tw.fondus.fews.adapter.pi.argument.extend;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The expand argument it used with CreateMapStackXmlAdapter.
 *
 * @author Brad Chen
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class MapStackArguments extends PiIOArguments {
	@Parameter( names = { "--duration", "-d" }, required = true, description = "The duration relative to the start time with start time." )
	private int duration;

	@Builder.Default
	@Parameter( names = { "--time-direction", "-td" }, description = "The time direction relative to time zero, support for end and start only, default is end." )
	private String direction = "end";

	@Builder.Default
	@Parameter( names = { "--time-step", "-ts" }, description = "The time step, support for day, hour and minute only, default is hour." )
	private String timeStep = "hour";

	@Builder.Default
	@Parameter( names = { "--time-multiplier", "-tm" }, description = "The time step multiplier, default is 1." )
	private int multiplier = 1;

	@Builder.Default
	@Parameter( names = { "--geo-datum", "-gt" }, description = "The geo datum of map stacks, default is 'WGS 1984'." )
	private String geoDatum = "WGS 1984";

	@Parameter( names = { "--name", "-n" }, required = true, description = "The file name pattern of the map stacks." )
	private String filePattern;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static MapStackArguments instance(){
		return MapStackArguments.builder().build();
	}
}
