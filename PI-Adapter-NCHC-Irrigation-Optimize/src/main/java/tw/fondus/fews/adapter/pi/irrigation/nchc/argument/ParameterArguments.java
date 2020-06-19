package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.splitter.CommaSplitter;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

import java.util.List;

/**
 * The parameter-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ParameterArguments extends PiBasicArguments {
	@Builder.Default
	@Parameter( names = { "--tdir", "-td" }, description = "The template folder, relative to the current working directory." )
	private String templatePath = "Template/";

	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The executable folder, relative to the current working directory." )
	private String executablePath = "Work/";

	@Parameter( names = { "-url" }, required = true, description = "The parameter API URL." )
	private String url;

	@Parameter( names = { "-token" }, required = true, description = "The parameter API authentication token." )
	private String token;

	@Parameter( names = { "--region", "-r" }, required = true, description = "The region of select." )
	private String region;

	@Parameter( names = { "--case", "-c" }, required = true, description = "The case parameter inside the region." )
	private String caseName;

	@Parameter( names = { "-hs" }, required = true, description = "The hydraulic structures list with comma, and order is fixed.",
			splitter = CommaSplitter.class )
	private List<String> hydraulicStructures;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static ParameterArguments instance(){
		return ParameterArguments.builder().build();
	}
}
