package tw.fondus.fews.adapter.pi.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.argument.BasicArguments;
import tw.fondus.fews.adapter.pi.argument.converter.DateTimeConverter;

/**
 * Basic arguments to run the command-Line interface.<br/>
 * Not included input and output files, if you need this, use {@link PiIOArguments} please.<br/>
 * If you want expand arguments, use extends to expand arguments please.
 * 
 * @author Brad Chen
 * @see BasicArguments
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PiBasicArguments extends BasicArguments {
	@Builder.Default
	@Parameter( names = { "--time", "-t" }, description = "The T0. TimeZone is UTC.", converter = DateTimeConverter.class )
	private DateTime timeZero = new DateTime();

	@Builder.Default
	@Parameter( names = { "--log", "-l" }, description = "The diagnostics log file name." )
	private String diagnostics = "Diagnostics.xml";

	@Builder.Default
	@Parameter( names = { "--ldir", "-ld" }, description = "The diagnostics folder, relative to the current working directory." )
	private String logPath = "Diagnostics/";

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PiBasicArguments instance(){
		return PiBasicArguments.builder().build();
	}
}
