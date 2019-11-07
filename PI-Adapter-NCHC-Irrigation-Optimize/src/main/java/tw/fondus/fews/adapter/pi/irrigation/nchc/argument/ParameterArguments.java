package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.converter.FileListConverter;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

import java.util.List;

/**
 * The parameter-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class ParameterArguments extends PiBasicArguments {
	@Parameter( names = { "--tdir", "-td" }, description = "The template folder, relative to the current working directory." )
	private String templatePath = "Template/";

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
			listConverter = FileListConverter.class )
	private List<String> hydraulicStructures;

	@Parameter( names = { "-wrt" }, required = true, description = "The water requirement target list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> waterRequirementTargets;

	@Parameter( names = { "-wrf" }, required = true, description = "The water requirement file list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> waterRequirementsFiles;
}
