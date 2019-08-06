package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.DateFormatUtils;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model pre-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = new PiIOArguments();
		new WuSacramentoPreAdapter().execute( args, arguments );
	}

	@Override
	protected String createModelInputContent( TimeSeriesArray array ) {
		StringJoiner content = new StringJoiner( StringUtils.BREAKLINE,
				Strman.append( array.getHeader().getLocationId(), StringUtils.BREAKLINE ),
				Strman.append( StringUtils.BREAKLINE, "-9999 -9999 -9999", StringUtils.BREAKLINE ));
		
		IntStream.range(0, array.size()).forEach( i -> {
			content.add(
					Strman.append( DateFormatUtils.format( array.getTime( i ), "ddMMyyyy HHmm ", TimeZone.getTimeZone( "GMT" ) ),
					String.valueOf( TimeSeriesLightUtils.getValue( array, i, 0.0F ) ))
					);
		});
		
		return content.toString();
	}
}
