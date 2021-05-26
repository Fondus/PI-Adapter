package tw.fondus.fews.adapter.pi.aws.storage.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.aws.storage.converter.PredefinePrefixConverter;
import tw.fondus.fews.adapter.pi.aws.storage.util.PredefinePrefix;

/**
 * Model adapter arguments for data export with S3 REST API.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ExportS3Arguments extends S3Arguments {
	@Builder.Default
	@Parameter( names = { "--object-prefix" }, description = "The prefix of storage object." )
	private String objectPrefix = "";

	@Builder.Default
	@Parameter( names = { "--predefine-prefix" }, description = "Use the predefine prefix of storage object.", converter = PredefinePrefixConverter.class )
	private PredefinePrefix predefinePrefix = PredefinePrefix.USER;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static ExportS3Arguments instance(){
		return ExportS3Arguments.builder().build();
	}
}
