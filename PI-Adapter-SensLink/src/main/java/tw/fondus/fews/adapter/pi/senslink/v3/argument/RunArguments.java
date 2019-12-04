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
public class RunArguments extends ExportArguments {
	@Parameter(names = { "--timeindex", "-ti" }, required = true, description = "The time index of the TimeSeriesArray index position")
	private int index;
	
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The minus duration relative to the current time.")
	private int duration;
}
