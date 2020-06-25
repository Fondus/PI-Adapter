package tw.fondus.fews.adapter.pi.rtc.nchc.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Model executable-adapter arguments for running NCHC RTC model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class RunArguments extends PreAdapterArguments {
	@Builder.Default
	@Parameter( names = { "--edir", "-ed" }, description = "The model executable directory path, relative to the current working directory." )
	private String executableDir = "Work/";

	@Parameter( names = { "--executable", "-e" }, required = true, description = "The model executable." )
	private List<String> executable;
	
	@Parameter( names = { "--tdir", "-td" }, required = true, description = "The template directory of model file." )
	private String templateDir;

	@Builder.Default
	@Parameter( names = { "--pname", "-pn" }, description = "The project name for backup output file name." )
	private String projectName = "ProjectName";
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static RunArguments instance(){
		return RunArguments.builder().build();
	}
}
