package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPostAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Model post-adapter for running NCHC MSFRM model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPostAdapter extends RainRunoffPostAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new MSFRMPostAdapter().execute( args, arguments );
	}

	@Override
	protected void parseModelOutputContent( Path outputPath, SimpleTimeSeriesContentHandler contentHandler,
			String parameter, String unit, long startTimeMillis, long timeStepMillis ) {
		String locationId = PathUtils.getNameWithoutExtension( outputPath );
		TimeSeriesLightUtils.addHeader( contentHandler, locationId, parameter, unit, timeStepMillis );
		
		List<String> fileLines = PathReader.readAllLines( outputPath );
		IntStream.range( 0, fileLines.size() ).forEach( i -> {
			String[] data = fileLines.get(i).trim().split( Strings.SPLIT_SPACE_MULTIPLE );
			
			TimeSeriesLightUtils.addValue( contentHandler, startTimeMillis + i * timeStepMillis, NumberUtils.create( data[2] ) );
		});
	}
}
