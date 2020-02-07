package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The model pre-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPreAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PiIOArguments arguments = new PiIOArguments();
		new LTFPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;

		try {
			// Check the XML exists
			Path rainfallXML = Prevalidated.checkExists(
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 0 ) ),
					"NCHC LTF PreAdapter: The XML file of rainfall is not exist." );

			Path waterLevelXML = Prevalidated.checkExists(
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 1 ) ),
					"NCHC LTF PreAdapter: The XML file of water level is not exist." );

			TimeSeriesArray rainfallTimeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( rainfallXML ).get( 0 );
			TimeSeriesArray waterLevelTimeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( waterLevelXML ).get( 0 );

			Preconditions.checkState( rainfallTimeSeriesArray.size() % 10 == 0,
					"NCHC LTF PreAdapter: The rainfall data are not divisible by 10 days." );
			Preconditions.checkState( waterLevelTimeSeriesArray.size() % 10 == 0,
					"NCHC LTF PreAdapter: The water level data are not divisible by 10 days." );

			logger.log( LogLevel.INFO, "NCHC LTF PreAdapter: Start create model input files." );

			this.writeModelInput( logger, inputPath, rainfallTimeSeriesArray, waterLevelTimeSeriesArray,
					modelArguments.getOutputs() );

			logger.log( LogLevel.INFO, "NCHC LTF PreAdapter: Finished create model input files." );

		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF PreAdapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF PreAdapter: Read XML has something faild!" );
		}
	}

	/**
	 * Write model input file.
	 * 
	 * @param logger
	 * @param inputPath
	 * @param rainfallArray
	 * @param waterLevelArray
	 */
	private void writeModelInput( PiDiagnosticsLogger logger, Path inputPath, TimeSeriesArray rainfallArray,
			TimeSeriesArray waterLevelArray, List<String> outputs ) {
		List<Float> tenDaysRainfall = new ArrayList<>();
		List<Float> tenDaysWaterLevel = new ArrayList<>();

		// Calculate the rainfall and water level
		IntStream.range( 0, rainfallArray.size() / 10 ).forEach( tenDays -> {
			float rainfall = 0;
			float waterLevel = 0;
			int waterLevelCount = 0;
			for ( int data = 0; data < 10; data++ ) {
				rainfall += TimeSeriesLightUtils.getValue( rainfallArray, (tenDays * 10) + data, 0 );
				if ( TimeSeriesLightUtils.getValue( waterLevelArray, (tenDays * 10) + data, 0 ) > 0 ) {
					waterLevel += TimeSeriesLightUtils.getValue( waterLevelArray, (tenDays * 10) + data, 0 );
					waterLevelCount++;
				}
			}

			tenDaysRainfall.add( rainfall );
			if ( waterLevelCount > 0 ) {
				tenDaysWaterLevel.add( waterLevel / waterLevelCount );
			} else {
				tenDaysWaterLevel.add( (float) 0 );
			}
		} );

		// Create the content and suffix
		StringJoiner rainfall = new StringJoiner( StringUtils.TAB, StringUtils.BLANK, StringUtils.BREAKLINE );
		StringJoiner waterLevel = new StringJoiner( StringUtils.TAB, StringUtils.BLANK, StringUtils.BREAKLINE );
		StringJoiner endLine = new StringJoiner( StringUtils.TAB, StringUtils.BREAKLINE, StringUtils.BLANK );
		IntStream.range( 0, tenDaysRainfall.size() ).forEach( tenDaysData -> {
			rainfall.add( Strman.append( String.valueOf( tenDaysRainfall.get( tenDaysData ) ) ) );
			waterLevel.add( Strman.append( String.valueOf( tenDaysWaterLevel.get( tenDaysData ) ) ) );
			endLine.add( Strman.append( String.valueOf( "-999" ) ) );
		} );

		try {
			StringJoiner rainfallContent = new StringJoiner( StringUtils.BREAKLINE, StringUtils.BLANK,
					endLine.toString() );
			StringJoiner waterLevelContent = new StringJoiner( StringUtils.BREAKLINE, StringUtils.BLANK,
					endLine.toString() );

			rainfallContent.add( rainfall.toString() );
			waterLevelContent.add( waterLevel.toString() );

			// Write the rainfall input
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, outputs.get( 0 ) ),
					rainfallContent.toString() );
			// Write the water level input
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, outputs.get( 1 ) ),
					waterLevelContent.toString() );

			// Write the time meta-information
			DateTime dateTime = new DateTime( rainfallArray.getEndTime() );
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, outputs.get( 2 ) ),
					TimeUtils.toString( dateTime, TimeUtils.YMD_NONSPLITE, TimeUtils.UTC8 ) );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF PreAdapter: Writing model input file has something wrong." );
		}
	}
}
