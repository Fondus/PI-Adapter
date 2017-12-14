package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffPostAdapter;

/**
 * Model post-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoPostAdapter extends RainRunoffPostAdapter {
	
	public static void main(String[] args) {
		PiArguments arguments = new PiArguments();
		new WuSacramentoPostAdapter().execute(args, arguments);
	}
	
	@Override
	protected void parseFileContent(File outputFile, SimpleTimeSeriesContentHandler contentHandler,
			String parameter, String unit,
			long startTimeMillis, long timeStepMillis) throws IOException {
		List<String> fileLines = Files.readAllLines(Paths.get(outputFile.getPath()), StandardCharsets.UTF_8);
		
		String locationId = fileLines.get(0);
		TimeSeriesUtils.fillPiTimeSeriesHeader(contentHandler, locationId, parameter, unit, timeStepMillis);
		
		IntStream.range(1, fileLines.size()).forEach(i -> {
			String[] datas = StringUtils.splitByWholeSeparator(fileLines.get(i), null);
			
			String time = datas[1];
			int timeSize = datas[1].length();
			switch (timeSize) {
				case 1:
					time = Strman.append("000", datas[1]);
					break;
				case 3:
					time = Strman.append("0", datas[1]);
					break;
			}
			
			String date = datas[0].trim();
			if ( datas[0].length() == 7 ){
				date = Strman.append("0", datas[0].trim());
			} 
			
			DateTime dateTime = TimeUtils.toDateTime( Strman.append(date, " ", time) , "ddMMyyyy HHmm", TimeUtils.UTC0);
			TimeSeriesUtils.addPiTimeSeriesValue(contentHandler, dateTime.getMillis(), Float.valueOf(datas[2]));
		});
	}
}
