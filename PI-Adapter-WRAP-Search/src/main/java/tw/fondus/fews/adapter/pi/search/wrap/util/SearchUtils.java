package tw.fondus.fews.adapter.pi.search.wrap.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.PiDateTime;
import tw.fondus.commons.fews.pi.config.xml.PiTimeStep;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.FileNamePattern;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.Pattern;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;

public class SearchUtils {
	/**
	 * 
	 * Determine area rainfall be up to the standard which level.
	 * 
	 * @param value
	 * @param rainfallLevelFile
	 * @return
	 */

	public static String determineRainfallIntensity( Double value, List<String> rainfallLevelFile ) {
		String rainfallIntensity = StringUtils.BLANK;

		for ( int i = 0; i < rainfallLevelFile.size(); i++ ) {
			if ( value > Double.valueOf( rainfallLevelFile.get( i ) ) ) {
				rainfallIntensity = Strman.append( "r", String.valueOf( rainfallLevelFile.get( i ) ) );
				break;

			}
		}
		return rainfallIntensity;

	}

	/**
	 * Get rainfall level file.
	 * 
	 * @param levelPath
	 * @return
	 * @throws IOException
	 */
	public static List<String> readQuantitativeRainfallLevel( String levelPath ) throws IOException {

		try {
			List<String> levelFile = Files.readAllLines( Paths.get( levelPath ) );

			List<String> rainfallLevelDataList = new ArrayList<String>();
			levelFile.forEach( data -> {
				rainfallLevelDataList.add( data.split( StringUtils.COMMA)[1] );
			} );
			return rainfallLevelDataList;

		} catch (IOException e) {
			throw new IOException( "Level File has something wrong." );

		}
	}

	/**
	 * Create the output mapstacks.xml.
	 * 
	 * @param exportPath
	 * @param town
	 * @param forecastTime
	 * @throws Exception
	 */
	public static void creatMapXml( String exportPath, String town, String forecastTime ) throws Exception {
		List<MapStack> mapStacksList = new ArrayList<>();

		MapStacks mapStacks = new MapStacks();

		MapStack mapStack = new MapStack();
		mapStack.setLocationId( Strman.append( "Grid_", town, "_Search" ) );
		mapStack.setParameterId( "Depth.simulated" );
		PiDateTime dateTime = new PiDateTime();
		dateTime.setDate( forecastTime.split( " " )[0] );
		dateTime.setTime( forecastTime.split( " " )[1] );
		mapStack.setStartDate( dateTime );
		mapStack.setEndDate( dateTime );
		PiTimeStep timeStep = new PiTimeStep();
		timeStep.setUnit( "hour" );
		timeStep.setMultiplier( 1 );
		mapStack.setTimeStep( timeStep );
		FileNamePattern Filepattern = new FileNamePattern();
		Pattern pattern = new Pattern();
		pattern.setFile( Strman.append( "Depth", "????", FileType.ASC.getExtension()) );
		Filepattern.setPattern( pattern );
		mapStack.setFile( Filepattern );
		mapStacksList.add( mapStack );
		mapStacks.setMapStacks( mapStacksList );
		mapStacks.setGeoDatum( "TWD 1997" );
		mapStacks.setTimeZone( "0.0" );
		mapStacks.setVersion( "1.2" );
		XMLUtils.toXML( new File( Strman.append( exportPath, StringUtils.PATH, "map", FileType.XML.getExtension() ) ),
				mapStacks );

	}
	

	/**
	 * Get long and return string format.
	 * 
	 * @param time
	 * @return
	 * @throws IOException
	 */
	public static String getStingTime( long time ) throws IOException {
		Date date = new Date();
		long startTime = time;
		date.setTime( startTime );
		String timeToStation = StringUtils.BLANK;
		try {
			timeToStation = TimeUtils.toString( date, TimeUtils.YMDHMS );
		} catch (ParseException e) {
			throw new IOException( "Trandsfor long to string has something wrong" );
		}
		return timeToStation;
	}
}
