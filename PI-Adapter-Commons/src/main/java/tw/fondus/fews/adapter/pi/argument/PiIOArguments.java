package tw.fondus.fews.adapter.pi.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.converter.FileListConverter;

/**
 * Standard arguments use for the included input, output files, parameter and unit to run the command-Line interface.<br/>
 * If you want expand arguments, use extends to expand arguments please.
 * 
 * @see PiBasicArguments
 * @author Brad Chen
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PiIOArguments extends PiBasicArguments {
	@Parameter( names = { "--input", "-i" }, required = true, description = "The input file list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> inputs;

	@Parameter( names = { "--output", "-o" }, required = true, description = "The output file list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> outputs;

	@Parameter( names = { "--parameter", "-p" }, description = "The parameter name of model output, use only when program need it." )
	private String parameter;

	@Parameter( names = { "--unit", "-u" }, description = "The unit name of model output, use only when program need it." )
	private String unit;
}
