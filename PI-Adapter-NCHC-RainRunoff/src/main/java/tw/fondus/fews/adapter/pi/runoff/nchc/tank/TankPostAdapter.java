package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPostAdapter;

/**
 * Model post-adapter for running NCHC Tank model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TankPostAdapter extends RainRunoffPostAdapter {
	
	public static void main(String[] args) {
		PiArguments arguments = new PiArguments();
		new TankPostAdapter().execute(args, arguments);
	}
	
	@Override
	protected void parseFileContent(File outputFile, SimpleTimeSeriesContentHandler contentHandler, String parameter,
			String unit, long startTimeMillis, long timeStepMillis) throws IOException {
		String locationId = FileUtils.getNameWithoutExt(outputFile);
		TimeSeriesUtils.fillPiTimeSeriesHeader(contentHandler, locationId, parameter, unit, timeStepMillis);
		
		List<String> fileLines = Files.readAllLines(Paths.get(outputFile.getPath()), StandardCharsets.UTF_8);
		IntStream.range(0, fileLines.size()).forEach(i -> {
			String[] datas = StringUtils.splitByWholeSeparator(fileLines.get(i), null);
			
			TimeSeriesUtils.addPiTimeSeriesValue(contentHandler, startTimeMillis + i * timeStepMillis, Float.valueOf(datas[1]));
		});
	}

}
