package tw.fondus.fews.adapter.pi.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.splitter.CommaSplitter;

import java.util.List;

/**
 * Standard arguments use for the included input, output files, parameter and unit to run the command-Line interface.<br/>
 * If you want expand arguments, use extends to expand arguments please.
 * 
 * @see PiBasicArguments
 * @author Brad Chen
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PiIOArguments extends PiBasicArguments {
	@Parameter( names = { "--input", "-i" }, required = true, description = "The input file list with comma, and order is fixed.",
			splitter = CommaSplitter.class )
	private List<String> inputs;

	@Parameter( names = { "--output", "-o" }, required = true, description = "The output file list with comma, and order is fixed.",
			splitter = CommaSplitter.class )
	private List<String> outputs;

	@Parameter( names = { "--parameter", "-p" }, description = "The parameter name of model output, use only when program need it." )
	private String parameter;

	@Parameter( names = { "--unit", "-u" }, description = "The unit name of model output, use only when program need it." )
	private String unit;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PiIOArguments instance(){
		return PiIOArguments.builder().build();
	}
}
