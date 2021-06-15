package tw.fondus.fews.adapter.pi.aws.storage.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Model adapter arguments for data exchange with S3 REST API.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class S3Arguments extends S3FolderArguments {
	@Parameter( names = { "--object" }, required = true, description = "The storage object." )
	private String object;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static S3Arguments instance(){
		return S3Arguments.builder().build();
	}
}
