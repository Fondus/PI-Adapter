package tw.fondus.fews.adapter.pi.irrigation.nchc.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.converter.FileListConverter;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.util.ArrayList;
import java.util.List;

/**
 * The senslink-adapter arguments for running NCHC irrigation-optimize model.
 *
 * @author Brad Chen
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class SensLinkArgument extends PiIOArguments {
	@Parameter( names = { "--edir", "-ed" }, description = "The executable folder, relative to the current working directory." )
	private String executablePath = "Work/";

	@Parameter( names = { "--duration", "-d" }, description = "The time step of water requirement." )
	private int duration = 30;

	@Parameter( names = { "-wrb" }, required = true, description = "The water requirement senslink id of base time." )
	private String waterRequirementTimeBase;

	@Parameter( names = { "-wrt" }, required = true, description = "The water requirement senslink id list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> waterRequirementTargets;

	@Parameter( names = { "-wrf" }, required = true, description = "The water requirement file list with comma, and order is fixed.",
			listConverter = FileListConverter.class )
	private List<String> waterRequirementsFiles;

	@Parameter( names = { "-ids" }, description = "The SensLink system ids will pull, if the pull flag set be true.",
			listConverter = FileListConverter.class )
	private List<String> sensLinkIds = new ArrayList<>();

	@Parameter( names = { "-pullFlag" }, description = "Pull the model input from the SensLink system ids or not." )
	private boolean isPullFromSensLinkFlag = false;

	@Parameter(names = { "--username", "-us" }, required = true, description = "The account username.")
	private String username;

	@Parameter(names = { "--password", "-pw" }, required = true, description = "The account password.")
	private String password;
}
