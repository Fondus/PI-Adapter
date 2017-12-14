package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.DateFormatUtils;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;

/**
 * Model pre-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiArguments arguments = new PiArguments();
		new WuSacramentoPreAdapter().execute(args, arguments);
	}
	
	@Override
	protected String createFileContent(TimeSeriesArray array) {
		StringJoiner content = new StringJoiner(StringUtils.BREAKLINE,
				Strman.append( array.getHeader().getLocationId(), StringUtils.BREAKLINE ),
				Strman.append(StringUtils.BREAKLINE, "-9999 -9999 -9999", StringUtils.BREAKLINE));
		
		IntStream.range(0, array.size()).forEach(i -> {
			content.add(
					Strman.append(DateFormatUtils.format(array.getTime(i), "ddMMyyyy HHmm ", TimeUtils.GMT0),
					String.valueOf( TimeSeriesUtils.getValue(array, i, 0.0F) ))
					);
		});
		
		return content.toString();
	}
}
