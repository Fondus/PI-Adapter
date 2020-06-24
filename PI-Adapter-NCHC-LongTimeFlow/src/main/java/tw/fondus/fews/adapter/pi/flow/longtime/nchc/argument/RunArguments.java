package tw.fondus.fews.adapter.pi.flow.longtime.nchc.argument;

import java.math.BigDecimal;
import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model executable-adapter arguments for running NCHC long time flow model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--edir",
			"-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;

	@Builder.Default
	@Parameter( names = { "--tdir", "-td" }, description = "The template directory of model file." )
	private String templateDir = "Template/";

	@Builder.Default
	@Parameter( names = { "--pname", "-pn" }, description = "The project name for backup output file name." )
	private String projectName = "ProjectName";

	@Builder.Default
	@Parameter( names = { "--fsteps", "-fs" }, description = "The forecast steps for running model(unit:10-days)." )
	private BigDecimal forecastSteps = new BigDecimal( "3" );

	@Builder.Default
	@Parameter( names = { "--osteps", "-os" }, description = "The observed steps for running model(unit:10-days)." )
	private BigDecimal observedSteps = new BigDecimal( "12" );

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static RunArguments instance() {
		return RunArguments.builder().build();
	}
}
