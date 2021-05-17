package tw.fondus.fews.adapter.pi.grid.correct.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Adapter arguments for running correct grid with feature threshold.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PiIOArguments {
	@Builder.Default
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The time-series duration should contain, it's used to accumulate.")
	private int duration = 12;

	@Builder.Default
	@Parameter(names = { "--threshold", "-ts" }, required = true, description = "The value threshold of feature.")
	private int threshold = 40;

	@Builder.Default
	@Parameter( names = { "--feature-dir", "-fd" }, description = "The features folder, relative to the current working directory." )
	private String featurePath = "Features/";

	@Parameter( names = { "--feature-file", "-ff" }, description = "The features file, should inside the features folder." )
	private String featureFile;

	public static RunArguments instance(){
		return RunArguments.builder().build();
	}
}
