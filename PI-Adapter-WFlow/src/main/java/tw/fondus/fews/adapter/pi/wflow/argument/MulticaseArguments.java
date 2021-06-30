package tw.fondus.fews.adapter.pi.wflow.argument;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;

/**
 * The arguments of WFlow model multi-case run.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class MulticaseArguments extends S3Arguments {
	@Builder.Default
	@Parameter( names = { "--pdir", "-pd" }, description = "The parameters folder, relative to the current working directory." )
	private String parameterPath = "Parameter/";
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static MulticaseArguments instance(){
		return MulticaseArguments.builder().build();
	}
}
