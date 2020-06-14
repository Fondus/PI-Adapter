package tw.fondus.fews.adapter.pi.commons;

import org.joda.time.DateTime;
import tw.fondus.commons.fews.pi.config.xml.PiDateTime;
import tw.fondus.commons.fews.pi.config.xml.PiTimeStep;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.FileNamePattern;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.Pattern;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.extend.MapStackArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * The commons adapter tools it used to create the meta-data PI-XML of map stacks by arguments.
 *
 * @author Brad Chen
 *
 */
public class CreateMapStackXmlAdapter extends PiCommandLineExecute {
	private static final String[] SUPPORTS_TIMESTEP = { "hour", "day", "minute" };
	private static final String[] SUPPORTS_DIRECTION = { "end", "start" };

	public static void main( String[] args ) {
		MapStackArguments arguments = MapStackArguments.instance();
		new CreateMapStackXmlAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		MapStackArguments modelArguments = (MapStackArguments) arguments;

		String direction = modelArguments.getDirection();
		String timeStep = modelArguments.getTimeStep();
		int multiplier = modelArguments.getMultiplier();
		if ( this.supportTimeStep( timeStep ) ){
			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with TimeStep: {}, Multiplier: {}.", timeStep, multiplier );

			String locationId = modelArguments.getInputs().get( 0 );
			String parameter = modelArguments.getParameter();
			String geoDatum = modelArguments.getGeoDatum();
			String filePattern = modelArguments.getFilePattern();
			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with LocationId: {}, ParameterId: {}, GeoDatum: {}, FilePattern: {}.", locationId, parameter, geoDatum, filePattern );

			// Create base on the direction
			if ( this.supportDirection( direction ) ){
				int duration = modelArguments.getDuration();
				DateTime start;
				DateTime end;
				if ( "start".equals( direction ) ) {
					end = modelArguments.getTimeZero();
					start = this.createTime( end, duration, timeStep, direction );
				} else {
					start = modelArguments.getTimeZero();
					end = this.createTime( start, duration, timeStep, direction );
				}
				logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with Start Time: {}, End Time: {}.", start.toString(), end.toString() );

				logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: Start create meta-information file." );
				MapStacks mapStacks = this.buildMapStack( filePattern, start, end, geoDatum, locationId, parameter, timeStep, multiplier );

				// Write the XML
				Path xmlPath = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
				try {
					XMLUtils.toXML( xmlPath, mapStacks );
				} catch (Exception e) {
					logger.log( LogLevel.ERROR, "CreateMapStackXmlAdapter: Write the XML has something wrong." );
				}

				logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: Finished create meta-information file." );

			} else {
				logger.log( LogLevel.WARN, "CreateMapStackXmlAdapter: The user select Time direction: {} not support.", direction );
			}
		} else {
			logger.log( LogLevel.WARN, "CreateMapStackXmlAdapter: The user select TimeStep: {} not support.", timeStep );
		}
	}

	/**
	 * Build the MapStacks object.
	 *
	 * @param filePattern file pattern
	 * @param start start time
	 * @param end end time
	 * @param geoDatum geo datum
	 * @param locationId location id
	 * @param parameter parameter
	 * @param timeStep time step
	 * @param multiplier multiplier
	 * @return map stacks XML bean
	 */
	private MapStacks buildMapStack( String filePattern, DateTime start, DateTime end, String geoDatum, String locationId, String parameter, String timeStep, int multiplier ){
		// Build part of Map Stack XML
		FileNamePattern namePattern = FileNamePattern.of( Pattern.of( filePattern ) );
		PiDateTime startDate = this.createPiDateTime( start );
		PiDateTime endDate = this.createPiDateTime( end );
		PiTimeStep piTimeStep = PiTimeStep.of( timeStep, multiplier );
		MapStack mapStack = MapStack.of( locationId, parameter, piTimeStep, startDate, endDate, namePattern );

		// Build Map Stack XML
		MapStacks mapStacks = new MapStacks();
		mapStacks.setGeoDatum( geoDatum );
		mapStacks.add( mapStack );
		return mapStacks;
	}

	/**
	 * Create the Pi data-time.
	 *
	 * @param time joda time
	 * @return map stack date time
	 */
	private PiDateTime createPiDateTime( DateTime time ){
		return PiDateTime.of( TimeLightUtils.toString( time, "yyyy-MM-dd", TimeLightUtils.UTC0 ), TimeLightUtils.toString( time, "HH:mm:ss", TimeLightUtils.UTC0 ) );
	}

	/**
	 * Check the time step support or not.
	 *
	 * @param timeStep time step
	 * @return is support time step or not
	 */
	private boolean supportTimeStep( String timeStep ){
		return this.support( SUPPORTS_TIMESTEP, timeStep );
	}

	/**
	 * Check the time direction support or not.
	 *
	 * @param direction direction
	 * @return is support direction or not
	 */
	private boolean supportDirection( String direction ){
		return this.support( SUPPORTS_DIRECTION, direction );
	}

	/**
	 * Check the target support with white list or not.
	 *
	 * @param whiteList white list
	 * @param target target
	 * @return is support or not
	 */
	private boolean support( String[] whiteList, String target ){
		return Arrays.asList( whiteList ).contains( target );
	}

	/**
	 * Create the end time by duration and time step.
	 *
	 * @param base base joda time
	 * @param duration string time duration
	 * @param timeStep string time step
	 * @return adjust time
	 */
	private DateTime createTime( DateTime base, int duration, String timeStep, String direction ){
		switch ( timeStep ){
			case "day":
				if ( direction.equals( SUPPORTS_DIRECTION[0] ) ){
					return base.plusDays( duration );
				} else {
					return base.minusDays( duration );
				}
			case "minute":
				if ( direction.equals( SUPPORTS_DIRECTION[0] ) ){
					return base.plusMinutes( duration );
				} else {
					return base.minusMinutes( duration );
				}
			default: // hour
				if ( direction.equals( SUPPORTS_DIRECTION[0] ) ){
					return base.plusHours( duration );
				} else {
					return base.minusHours( duration );
				}
		}
	}
}
