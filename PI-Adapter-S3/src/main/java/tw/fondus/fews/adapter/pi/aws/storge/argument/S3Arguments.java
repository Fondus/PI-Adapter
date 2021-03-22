package tw.fondus.fews.adapter.pi.aws.storge.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.util.Objects;

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
public class S3Arguments extends PiIOArguments {
	@Parameter( names = { "--host", "-host" }, description = "The S3 host URL." )
	private String host;

	@Parameter( names = { "--bucket" }, required = true, description = "The storage bucket." )
	private String bucket;

	@Parameter( names = { "--object" }, required = true, description = "The storage object." )
	private String object;

	@Parameter( names = { "--username", "-us" }, required = true, description = "The account username." )
	private String username;

	@Parameter( names = { "--password", "-pw" }, required = true, description = "The account password." )
	private String password;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static S3Arguments instance(){
		return S3Arguments.builder().build();
	}

	public String getHost(){
		return Objects.requireNonNull( this.host, "Host can not be null." );
	}

	public String getBucket(){
		return Objects.requireNonNull( this.bucket, "Bucket can not be null." );
	}

	public String getObject(){
		return Objects.requireNonNull( this.object, "Object can not be null." );
	}

	public String getUsername(){
		return Objects.requireNonNull( this.username, "Username can not be null." );
	}

	public String getPassword(){
		return Objects.requireNonNull( this.password, "Password can not be null." );
	}
}
