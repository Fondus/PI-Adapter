package tw.fondus.fews.adapter.pi.aws.storage.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.aws.storage.converter.PredefinePrefixConverter;
import tw.fondus.fews.adapter.pi.aws.storage.util.PredefinePrefix;

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
public class S3FolderArguments extends PiIOArguments {
	@Parameter( names = { "--host", "-host" }, required = true, description = "The S3 host URL." )
	private String host;

	@Parameter( names = { "--bucket" }, required = true, description = "The storage bucket." )
	private String bucket;

	@Parameter( names = { "--username", "-us" }, required = true, description = "The account username." )
	private String username;

	@Parameter( names = { "--password", "-pw" }, required = true, description = "The account password." )
	private String password;

	@Builder.Default
	@Parameter( names = { "--object-prefix" }, description = "The prefix of storage object." )
	private String objectPrefix = "";

	@Builder.Default
	@Parameter( names = { "--predefine-prefix" }, description = "Use the predefine prefix of storage object.", converter = PredefinePrefixConverter.class )
	private PredefinePrefix predefinePrefix = PredefinePrefix.USER;

	@Builder.Default
	@Parameter( names = { "--bucket-create" }, description = "Should create storage bucket or not." )
	private boolean create = false;

	@Builder.Default
	@Parameter( names = { "--file-prefix" }, description = "The allow prefix of files in the folder." )
	private String filePrefix = Strings.EMPTY;

	@Builder.Default
	@Parameter( names = { "--file-suffix" }, description = "The allow extension suffix without dot of files in the folder." )
	private String fileSuffix = Strings.EMPTY;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static S3FolderArguments instance(){
		return S3FolderArguments.builder().build();
	}
}
