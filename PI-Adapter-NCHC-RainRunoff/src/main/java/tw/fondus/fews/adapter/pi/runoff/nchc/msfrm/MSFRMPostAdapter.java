package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPostAdapter;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model post-adapter for running NCHC MSFRM model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMPostAdapter extends RainRunoffPostAdapter {
	
	public static void main(String[] args) {
		PiIOArguments arguments = new PiIOArguments();
		new MSFRMPostAdapter().execute( args, arguments );
	}

	@Override
	protected void parseModelOutputContent( Path outputPath, SimpleTimeSeriesContentHandler contentHandler,
			String parameter, String unit, long startTimeMillis, long timeStepMillis ) throws IOException {
		String locationId = PathUtils.getNameWithoutExtension( outputPath );
		TimeSeriesLightUtils.fillPiTimeSeriesHeader( contentHandler, locationId, parameter, unit, timeStepMillis );
		
		List<String> fileLines = PathUtils.readAllLines( outputPath );
		IntStream.range(0, fileLines.size()).forEach( i -> {
			String[] datas = StringUtils.splitByWholeSeparator(fileLines.get(i), null);
			
			TimeSeriesLightUtils.addPiTimeSeriesValue( contentHandler, startTimeMillis + i * timeStepMillis, Float.valueOf( datas[2] ) );
		});
	}
}
