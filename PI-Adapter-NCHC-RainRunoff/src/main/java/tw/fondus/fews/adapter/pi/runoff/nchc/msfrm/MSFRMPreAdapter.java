package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPreAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.util.StringJoiner;
import java.util.stream.IntStream;

/**
 * Model pre-adapter for running NCHC MSFRM model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class MSFRMPreAdapter extends RainRunoffPreAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new MSFRMPreAdapter().execute( args, arguments );
	}

	@Override
	protected String createModelInputContent( TimeSeriesArray array ) {
		StringJoiner content = new StringJoiner( Strings.BREAKLINE,
				Strings.EMPTY,
				Strman.append( Strings.BREAKLINE, "-999 -999     ! INDICATOR OF IMPORTING DATA NECESSARY", Strings.BREAKLINE ));
		
		IntStream.range( 0, array.size() ).forEach(i -> {
			if ( i == 0 ) {
				content.add( Strman.append( String.valueOf(i + 1), Strings.SPLIT_TAB, TimeSeriesLightUtils.getValue(array, i, Numbers.ZERO ).toString(), Strings.SPLIT_TAB, "! TIME ; OBSERVED RAINFALL"));
			} else {
				content.add( Strman.append( String.valueOf(i + 1), Strings.SPLIT_TAB, TimeSeriesLightUtils.getValue(array, i, Numbers.ZERO ).toString() ) );
			}
		});
		
		return content.toString();
	}
}
