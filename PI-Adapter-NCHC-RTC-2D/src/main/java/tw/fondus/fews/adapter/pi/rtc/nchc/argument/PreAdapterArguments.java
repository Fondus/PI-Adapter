package tw.fondus.fews.adapter.pi.rtc.nchc.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NCHC RR model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PreAdapterArguments extends PiIOArguments{
	@Parameter(names = { "--forecast", "-f" }, required = true, description = "The number of forecasting for the model.")
	private int forecast;
	
	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PreAdapterArguments instance(){
		return PreAdapterArguments.builder().build();
	}
}
