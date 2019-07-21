package tw.fondus.fews.adapter.pi.senslink.v2.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model adapter arguments for data exchange with SensLink 2.0.
 * 
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class RunArguments extends PiIOArguments {
	@Parameter(names = { "--timeindex", "-ti" }, required = true, description = "The time index of the TimeSeriesArray index position")
	private int index;
	
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The minus duration relative to the current time.")
	private int duration;
	
	@Parameter(names = { "--username", "-us" }, required = true, description = "The account username.")
	private String username;
	
	@Parameter(names = { "--password", "-pw" }, required = true, description = "The account password.")
	private String password;
	
	@Parameter(names = { "--server", "-s" }, required = true, description = "The target server, value can be 0 or 1 only.")
	private int server;
}
