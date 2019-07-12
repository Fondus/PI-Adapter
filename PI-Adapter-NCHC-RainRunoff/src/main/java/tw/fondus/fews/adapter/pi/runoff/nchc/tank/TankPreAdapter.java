package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model pre-adapter for running NCHC Tank model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TankPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = new PiIOArguments();
		new TankPreAdapter().execute( args, arguments );
	}

	@Override
	protected String createModelInputContent( TimeSeriesArray array ) {
		StringJoiner content = new StringJoiner(StringUtils.BREAKLINE,
				StringUtils.BLANK,
				Strman.append(StringUtils.BREAKLINE, "-999"));
		
		IntStream.range(0, array.size()).forEach(i -> {		
			content.add( String.valueOf( TimeSeriesLightUtils.getValue(array, i, 0.0F) ));
		});
		
		return content.toString();
	}
}
