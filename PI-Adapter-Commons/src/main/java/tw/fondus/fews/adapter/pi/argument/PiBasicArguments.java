package tw.fondus.fews.adapter.pi.argument;

import org.joda.time.DateTime;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.BasicArguments;
import tw.fondus.fews.adapter.pi.argument.converter.DateTimeConveter;

/**
 * Basic arguments to run the command-Line interface.<br/>
 * Not included input and output files, if you need this, use {@link PiIOArguments} please.<br/>
 * If you want expand arguments, use extends to expand arguments please.
 * 
 * @see PiIOArguments
 * @author Brad Chen
 * @see BasicArguments
 */
@Data
@EqualsAndHashCode( callSuper=false )
public class PiBasicArguments extends BasicArguments {
	@Parameter( names = { "--time", "-t" }, description = "The T0. TimeZone is UTC.", converter = DateTimeConveter.class )
	private DateTime timeZero = new DateTime();

	@Parameter( names = { "--log", "-l" }, description = "The diagnostics log file name." )
	private String dagnostics = "Diagnostics.xml";

	@Parameter( names = { "--ldir", "-ld" }, description = "The diagnostics folder, relative to the current working directory." )
	private String logPath = "Diagnostics/";
}
