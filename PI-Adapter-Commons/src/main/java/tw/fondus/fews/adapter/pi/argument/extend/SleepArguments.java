package tw.fondus.fews.adapter.pi.argument.extend;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

/**
 * Model adapter arguments for sleep millisecond with FEWS.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class SleepArguments extends PiBasicArguments {
	@Parameter( names = { "--sleep" }, required = true, description = "The millisecond of sleep time." )
	private long sleep;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static SleepArguments instance(){
		return SleepArguments.builder().build();
	}
}
