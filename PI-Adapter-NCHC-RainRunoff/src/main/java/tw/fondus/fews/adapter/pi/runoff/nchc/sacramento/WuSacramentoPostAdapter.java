package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import org.joda.time.DateTime;
import strman.Strman;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPostAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Model post-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoPostAdapter extends RainRunoffPostAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new WuSacramentoPostAdapter().execute(args, arguments);
	}

	@Override
	protected void parseModelOutputContent( Path outputPath, SimpleTimeSeriesContentHandler contentHandler,
			String parameter, String unit, long startTimeMillis, long timeStepMillis ) {
		List<String> fileLines = PathReader.readAllLines( outputPath );
		
		String locationId = fileLines.get(0);
		TimeSeriesLightUtils.addHeader( contentHandler, locationId, parameter, unit, timeStepMillis );
		
		IntStream.range( 1, fileLines.size() ).forEach( i -> {
			String[] data = fileLines.get(i).trim().split( Strings.SPLIT_SPACE_MULTIPLE );
			
			String time = data[1];
			int timeSize = data[1].length();
			switch ( timeSize ) {
				case 1:
					time = Strman.append( "000", data[1]);
					break;
				case 3:
					time = Strman.append( "0", data[1]);
					break;
			}
			
			String date = data[0].trim();
			if ( data[0].length() == 7 ){
				date = Strman.append("0", data[0].trim());
			} 
			
			DateTime dateTime = JodaTimeUtils.toDateTime( Strman.append( date, " ", time ) , "ddMMyyyy HHmm" );
			TimeSeriesLightUtils.addValue( contentHandler, dateTime.getMillis(), NumberUtils.create( data[2] ) );
		});
	}
}
