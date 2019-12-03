package tw.fondus.fews.adapter.pi.argument.extend;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joda.time.DateTime;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.argument.converter.DateTimeConveter;

/**
 * The expand argument it used with CreateMapStackXmlAdapter.
 *
 * @author Brad Chen
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class MapStackArguments extends PiIOArguments {
	@Parameter( names = { "--start", "-st" }, description = "The start date time, format is yyyyMMddHHmm. TimeZone is UTC.", converter = DateTimeConveter.class )
	private DateTime start = new DateTime();

	@Parameter( names = { "--duration", "-d" }, required = true, description = "The duration relative to the start time with start time." )
	private int duration;

	@Parameter( names = { "--time-step", "-ts" }, description = "The time step, support for day, hour and minute only, default is hour." )
	private String timeStep = "hour";

	@Parameter( names = { "--time-multiplier", "-tm" }, description = "The time step multiplier, default is 1." )
	private int multiplier = 1;

	@Parameter( names = { "--geo-datum", "-gt" }, description = "The geo datum of map stacks, default is 'WGS 1984'." )
	private String geoDatum = "WGS 1984";

	@Parameter( names = { "--name", "-n" }, required = true, description = "The file name pattern of the map stacks." )
	private String filePattern;
}
