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
import java.util.stream.Stream;

/**
 * The commons adapter tools it used to create the meta-data PI-XML of map stacks by arguments.
 *
 * @author Brad Chen
 *
 */
public class CreateMapStackXmlAdapter extends PiCommandLineExecute {
	private static final String[] SUPPORTS = { "hour", "day", "minute" };

	public static void main( String[] args ) {
		MapStackArguments arguments = new MapStackArguments();
		new CreateMapStackXmlAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		MapStackArguments modelArguments = (MapStackArguments) arguments;

		String timeStep = modelArguments.getTimeStep();
		int multiplier = modelArguments.getMultiplier();
		if ( this.supportTimeStep( timeStep ) ){
			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with TimeStep: {}, Multiplier: {}.", timeStep, String.valueOf( multiplier ) );

			String locationId = modelArguments.getInputs().get( 0 );
			String parameter = modelArguments.getParameter();
			String geoDatum = modelArguments.getGeoDatum();
			String filePattern = modelArguments.getFilePattern();
			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with LocationId: {}, ParameterId: {}, GeoDatum: {}, FilePattern: {}.", locationId, parameter, geoDatum, filePattern );

			DateTime start = modelArguments.getStart();
			int duration = modelArguments.getDuration();
			DateTime end = this.createEndTime( start, duration, timeStep );
			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: The meta-information with Start Time: {}, End Time: {}.", start.toString(), end.toString() );

			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: Start create meta-information file." );
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

			// Write the XML
			Path xmlPath = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
			try {
				XMLUtils.toXML( xmlPath, mapStacks );
			} catch (Exception e) {
				logger.log( LogLevel.ERROR, "CreateMapStackXmlAdapter: Write the XML has something wrong." );
			}

			logger.log( LogLevel.INFO, "CreateMapStackXmlAdapter: Finished create meta-information file." );

		} else {
			logger.log( LogLevel.WARN, "CreateMapStackXmlAdapter: The user select TimeStep: {} not support.", timeStep );
		}
	}

	/**
	 * Create the Pi data-time.
	 *
	 * @param time
	 * @return
	 */
	private PiDateTime createPiDateTime( DateTime time ){
		return PiDateTime.of( TimeLightUtils.toString( time, "yyyy-MM-dd", TimeLightUtils.UTC0 ), TimeLightUtils.toString( time, "HH:mm:ss", TimeLightUtils.UTC0 ) );
	}

	/**
	 * Check the time step support or not.
	 *
	 * @param timeStep
	 * @return
	 */
	private boolean supportTimeStep( String timeStep ){
		return Stream.of( SUPPORTS ).anyMatch( support -> support.equals( timeStep ) );
	}

	/**
	 * Create the end time by duration and time step.
	 *
	 * @param start
	 * @param duration
	 * @param timeStep
	 * @return
	 */
	private DateTime createEndTime( DateTime start, int duration, String timeStep ){
		switch ( timeStep ){
			case "day":
				return start.plusDays( duration );
			case "minute":
				return start.plusMinutes( duration );
			default: // hour
				return start.plusHours( duration );
		}
	}
}
