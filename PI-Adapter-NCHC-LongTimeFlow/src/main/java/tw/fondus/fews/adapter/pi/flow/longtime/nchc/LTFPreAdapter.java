package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.StringJoiner;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.commons.util.time.TimeFormats;
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
@SuppressWarnings( "rawtypes" )
public class LTFPreAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PiIOArguments arguments = PiIOArguments.instance();
		new LTFPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;

		try {
			// Check the XML exists
			Path rainfallXML = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
					"NCHC LTF PreAdapter: The XML file of rainfall is not exist." );

			Path waterLevelXML = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 1 ) ),
					"NCHC LTF PreAdapter: The XML file of water level is not exist." );

			TimeSeriesArray rainfallTimeSeriesArray = TimeSeriesLightUtils.read( rainfallXML ).get( 0 );
			TimeSeriesArray waterLevelTimeSeriesArray = TimeSeriesLightUtils.read( waterLevelXML ).get( 0 );

			Preconditions.checkState( rainfallTimeSeriesArray.size() % 10 == 0,
					"NCHC LTF PreAdapter: The rainfall data are not divisible by 10 days." );
			Preconditions.checkState( waterLevelTimeSeriesArray.size() % 10 == 0,
					"NCHC LTF PreAdapter: The water level data are not divisible by 10 days." );

			logger.log( LogLevel.INFO, "NCHC LTF PreAdapter: Start create model input files." );

			this.writeModelInput( logger, inputPath, rainfallTimeSeriesArray, waterLevelTimeSeriesArray,
					modelArguments.getOutputs() );

			logger.log( LogLevel.INFO, "NCHC LTF PreAdapter: Finished create model input files." );

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
		for ( int tenDays = 0; tenDays < rainfallArray.size() / 10; tenDays++ ) {
			float rainfall = 0;
			int rainfallCount = 0;
			float waterLevel = 0;
			int waterLevelCount = 0;
			for ( int data = 0; data < 10; data++ ) {
				if ( NumberUtils.greater( TimeSeriesLightUtils.getValue( rainfallArray, (tenDays * 10) + data ),
						BigDecimal.ZERO ) ) {
					rainfall += TimeSeriesLightUtils.getValue( rainfallArray, (tenDays * 10) + data ).floatValue();
					rainfallCount++;
				}

				if ( NumberUtils.greater( TimeSeriesLightUtils.getValue( waterLevelArray, (tenDays * 10) + data ),
						BigDecimal.ZERO ) ) {
					waterLevel += TimeSeriesLightUtils.getValue( waterLevelArray, (tenDays * 10) + data ).floatValue();
					waterLevelCount++;
				}
			}

			if ( rainfallCount > 0 ) {
				tenDaysRainfall.add( (rainfall / rainfallCount) * 10 );
			} else {
				tenDaysRainfall.add( (float) 0 );
			}

			if ( waterLevelCount > 0 ) {
				tenDaysWaterLevel.add( waterLevel / waterLevelCount );
			} else {
				tenDaysWaterLevel.add( (float) 0 );
			}
		}

		tenDaysRainfall = this.interpolatedMissingValue( tenDaysRainfall );
		tenDaysWaterLevel = this.interpolatedMissingValueByAverage( tenDaysWaterLevel );

		// Create the content and suffix
		StringJoiner rainfall = new StringJoiner( Strings.TAB, Strings.BLANK, Strings.BREAKLINE );
		StringJoiner waterLevel = new StringJoiner( Strings.TAB, Strings.BLANK, Strings.BREAKLINE );
		StringJoiner endLine = new StringJoiner( Strings.TAB, Strings.BREAKLINE, Strings.BLANK );
		for ( int tenDaysData = 0; tenDaysData < tenDaysRainfall.size(); tenDaysData++ ) {
			rainfall.add( String.valueOf( tenDaysRainfall.get( tenDaysData ) ) );
			waterLevel.add( String.valueOf( tenDaysWaterLevel.get( tenDaysData ) ) );
			endLine.add( String.valueOf( "-999" ) );
		}

		StringJoiner rainfallContent = new StringJoiner( Strings.BREAKLINE, Strings.BLANK, endLine.toString() );
		StringJoiner waterLevelContent = new StringJoiner( Strings.BREAKLINE, Strings.BLANK, endLine.toString() );

		rainfallContent.add( rainfall.toString() );
		waterLevelContent.add( waterLevel.toString() );

		// Write the rainfall input
		PathWriter.write( inputPath.resolve( outputs.get( 0 ) ), rainfallContent.toString() );
		// Write the water level input
		PathWriter.write( inputPath.resolve( outputs.get( 1 ) ), waterLevelContent.toString() );

		// Write the time meta-information
		DateTime dateTime = new DateTime( rainfallArray.getEndTime() );
		PathWriter.write( inputPath.resolve( outputs.get( 2 ) ),
				JodaTimeUtils.toString( dateTime, TimeFormats.YMD_UNDIVIDED, JodaTimeUtils.UTC8 ) );
	}

	/**
	 * Interpolate the missing value.
	 * 
	 * @param list
	 * @return
	 */
	private List<Float> interpolatedMissingValue( List<Float> list ) {
		/** fill start and end value if missing **/
		if ( list.get( 0 ) == 0 ) {
			list.set( 0, list.get( 1 ) );
		}
		if ( list.get( list.size() - 1 ) == 0 ) {
			list.set( list.size() - 1, list.get( list.size() - 2 ) );
		}

		/** fill missing value by average **/
		for ( int i = 1; i < list.size() - 1; i++ ) {
			if ( list.get( i ) == 0 ) {
				list.set( i, (list.get( i - 1 ) + list.get( i + 1 )) / 2 );
			}
		}

		return list;
	}
	
	/**
	 * Interpolate the missing value by average.
	 * 
	 * @param list
	 * @return
	 */
	private List<Float> interpolatedMissingValueByAverage( List<Float> list ) {
		OptionalDouble optAverage = list.stream().filter( v -> v != 0 ).mapToDouble( v -> v ).average();
		optAverage.ifPresent( average -> {
			for ( int i = 0; i < list.size(); i++ ) {
				if ( list.get( i ) == 0 ) {
					list.set( i, (float) average );
				}
			}
		} );
		return list;
	}
}
