package tw.fondus.fews.adapter.pi.search.wrap.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.PiDateTime;
import tw.fondus.commons.fews.pi.config.xml.PiTimeStep;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.FileNamePattern;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.Pattern;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.DateUtils;
import tw.fondus.commons.util.time.TimeFormats;

/**
 * The search tools.
 * 
 * @author shepherd
 * @author Brad Chen :improve the code
 */
public class SearchUtils {
	/**
	 * Determine area rainfall be up to the standard which level.
	 * 
	 * @param value
	 * @param thresholds
	 * @return
	 */
	public static Optional<String> determineRainfallIntensity( float value, List<Integer> thresholds ) {
		return thresholds.stream()
				.filter( threshold -> Double.valueOf( value ) > threshold )
				.map( threshold -> Strman.append( "r", String.valueOf( threshold ) ) )
				.findFirst();
	}

	/**
	 * Read rainfall threshold file.
	 * 
	 * @param thresholdPath
	 * @return
	 * @throws IOException
	 */
	public static List<Integer> readQuantitativeRainfallThreshold( Path thresholdPath ) throws IOException {
		return PathReader.readAllLines( thresholdPath ).stream()
			.map( line -> line.split( Strings.COMMA )[1] )
			.map( thresholdString -> Integer.valueOf( thresholdString ) )
			.collect( Collectors.toList() );
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
		PiDateTime dateTime = PiDateTime.of( forecastTime.split( " " )[0], forecastTime.split( " " )[1] );
		PiTimeStep timeStep = PiTimeStep.of( "hour", 1 );
		FileNamePattern filePattern = FileNamePattern.of( Pattern.of( Strman.append( "Depth", "????", FileType.ASC.getExtension()) ) );
		
		MapStack mapStack = MapStack.of( Strman.append( "Grid_", town, "_Search" ),
				"Depth.simulated", timeStep, dateTime, dateTime, filePattern );
		
		MapStacks mapStacks = new MapStacks();
		mapStacks.add( mapStack );
		mapStacks.setGeoDatum( "TWD 1997" );
		mapStacks.setTimeZone( "0.0" );
		
		XMLUtils.toXML( new File( Strman.append( exportPath, Strings.PATH, "map", FileType.XML.getExtension() ) ),
				mapStacks );
	}
	
	/**
	 * Get long and return string format.
	 * 
	 * @param time
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static String getStringTime( long time ) throws IOException, ParseException {
		return DateUtils.toString( new Date( time ), TimeFormats.YMDHMS );
	}
}
