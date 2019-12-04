package tw.fondus.fews.adapter.pi.senslink.v3.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model adapter arguments for data exchange with SensLink 3.0.
 *
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class ExportArguments extends PiIOArguments {
	@Parameter(names = { "--timestart", "-ts" }, description = "The start index of the TimeSeriesArray index position")
	private int start = 0;

	@Parameter(names = { "--username", "-us" }, required = true, description = "The account username.")
	private String username;

	@Parameter(names = { "--password", "-pw" }, required = true, description = "The account password.")
	private String password;
}
