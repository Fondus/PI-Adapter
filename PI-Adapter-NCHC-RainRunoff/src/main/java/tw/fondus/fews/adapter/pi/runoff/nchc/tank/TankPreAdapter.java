package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;

/**
 * Model pre-adapter for running NCHC Tank model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TankPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiArguments arguments = new PiArguments();
		new TankPreAdapter().execute(args, arguments);
	}
	
	@Override
	protected String createFileContent(TimeSeriesArray array) {
		StringJoiner content = new StringJoiner(StringUtils.BREAKLINE,
				StringUtils.BLANK,
				Strman.append(StringUtils.BREAKLINE, "-999"));
		
		IntStream.range(0, array.size()).forEach(i -> {		
			content.add(String.valueOf( TimeSeriesUtils.getValue(array, i, 0.0F) ));
		});
		
		return content.toString();
	}
}
