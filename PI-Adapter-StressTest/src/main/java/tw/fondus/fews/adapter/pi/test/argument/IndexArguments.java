package tw.fondus.fews.adapter.pi.test.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The adapter arguments used index for forward the time content.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class IndexArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--start", "-s" }, description = "The start index position relative to used index with time content." )
	private int start = -2;

	@Builder.Default
	@Parameter( names = { "--end", "-e" }, description = "The end index position relative to used index with time content." )
	private int end = 6;

	@Parameter( names = { "--length", "-length" }, required = true, description = "The length of time content." )
	private int length;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static IndexArguments instance(){
		return IndexArguments.builder().build();
	}
}
