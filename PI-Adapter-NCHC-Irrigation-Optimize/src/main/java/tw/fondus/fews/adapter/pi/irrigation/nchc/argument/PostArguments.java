package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.splitter.CommaSplitter;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.util.List;

/**
 * The post-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PostArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--duration", "-d" }, description = "The time duration." )
	private long duration = 3600000;

	@Parameter( names = { "--sub-location", "-sl" }, required = true, description = "The sub output location ID." )
	private String subLocationId;

	@Parameter( names = { "--three-location", "-tl" }, required = true, description = "The three output location IDs with comma.",
			splitter = CommaSplitter.class )
	private List<String> threeLocationIds;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PostArguments instance(){
		return PostArguments.builder().build();
	}
}
