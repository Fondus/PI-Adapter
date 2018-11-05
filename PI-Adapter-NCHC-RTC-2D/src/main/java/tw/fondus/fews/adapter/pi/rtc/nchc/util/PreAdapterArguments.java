package tw.fondus.fews.adapter.pi.rtc.nchc.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model pre-adapter arguments for running NCHC RR model.
 * 
 * @author Chao
 *
 */
public class PreAdapterArguments extends PiArguments{
	@Parameter(names = { "--forecast", "-f" }, required = true, description = "The number of forecasting for the model.")
	private int forecast;

	public int getForecast() {
		return forecast;
	}

	public void setForecast( int forecast ) {
		this.forecast = forecast;
	}
	
}
