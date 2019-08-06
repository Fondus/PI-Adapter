package tw.fondus.fews.adapter.pi.rtc.nchc.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NCHC RR model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PreAdapterArguments extends PiIOArguments{
	@Parameter(names = { "--forecast", "-f" }, required = true, description = "The number of forecasting for the model.")
	private int forecast;
}
