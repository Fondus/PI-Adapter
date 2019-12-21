package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.converter.FileListConverter;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.util.List;

/**
 * The post-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PostArguments extends PiIOArguments {
	@Parameter( names = { "--duration", "-d" }, description = "The time duration." )
	private long duration = 3600000;

	@Parameter( names = { "--sub-location", "-sl" }, required = true, description = "The sub output location ID." )
	private String subLocationId;

	@Parameter( names = { "--three-location", "-tl" }, required = true, description = "The three output location IDs with comma.",
			listConverter = FileListConverter.class )
	private List<String> threeLocationIds;
}
