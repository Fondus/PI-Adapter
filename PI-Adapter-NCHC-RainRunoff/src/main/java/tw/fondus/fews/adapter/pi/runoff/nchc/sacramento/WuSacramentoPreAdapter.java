package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.util.StringJoiner;
import java.util.stream.IntStream;

/**
 * Model pre-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class WuSacramentoPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new WuSacramentoPreAdapter().execute( args, arguments );
	}

	@Override
	protected String createModelInputContent( TimeSeriesArray array ) {
		StringJoiner content = new StringJoiner( Strings.BREAKLINE,
				Strman.append( array.getHeader().getLocationId(), Strings.BREAKLINE ),
				Strman.append( Strings.BREAKLINE, "-9999 -9999 -9999", Strings.BREAKLINE ));
		
		IntStream.range(0, array.size()).forEach( i ->
			content.add(
					Strman.append( JodaTimeUtils.toString( JodaTimeUtils.toDateTime( array.getTime( i ) ), "ddMMyyyy HHmm " ),
					String.valueOf( TimeSeriesLightUtils.getValue( array, i, Numbers.ZERO ) ))
					)
		);
		
		return content.toString();
	}
}
