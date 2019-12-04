package tw.fondus.fews.adapter.pi.commons;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;

/**
 * The commons adapter tools it used to adjust time of input PI-XML by base PI-XML start time.
 *
 * @author Brad Chen
 *
 */
public class AdjustTimeContentAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PiIOArguments arguments = new PiIOArguments();
		new AdjustTimeContentAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"AdjustTimeContentAdapter: The input XML file is not exist." );

		Path baseXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 1 ) ),
				"AdjustTimeContentAdapter: The base XML file is not exist." );

		try {
			logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: Read the PI-XML data." );
			TimeSeriesArrays inputTimeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			TimeSeriesArrays baseTimeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( baseXML );

			long inputTimeStep = inputTimeSeriesArrays.getCommonTimeStepMillis();
			long baseTimeStep = baseTimeSeriesArrays.getCommonTimeStepMillis();
			DateTime startTime = new DateTime( baseTimeSeriesArrays.getPeriod().getStartTime() );

			logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: The input time step millis: {}, base time step millis: {}.", String.valueOf( inputTimeStep ), String.valueOf( baseTimeStep ) );
			if ( inputTimeStep != baseTimeStep ){
				logger.log( LogLevel.WARN, "AdjustTimeContentAdapter: The input time step and base time step not equal, adapter not need to do." );
			} else {
				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: Start the adjust the with start time: {}.", startTime.toString() );

				SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
				inputTimeSeriesArrays.forEach( array -> {
					TimeSeriesHeader header = array.getHeader();
					String locationId = header.getLocationId();
					String parameter = header.getParameterId();
					String unit = header.getUnit();

					if ( inputTimeStep == 86400000L ){
						TimeSeriesLightUtils.fillPiTimeSeriesHeaderIrregular( handler, locationId, parameter, unit );
					} else {
						TimeSeriesLightUtils.fillPiTimeSeriesHeader( handler, locationId, parameter, unit, inputTimeStep );
					}

					IntStream.range( 0, array.size() ).forEach( i -> {
						TimeSeriesLightUtils.addPiTimeSeriesValue( handler,
								startTime.plus( i * inputTimeStep ).getMillis(), TimeSeriesLightUtils.getValue( array, i ) );
					} );
				} );

				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: Write the adjust PI-XML." );
				Path outputXML = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
				TimeSeriesLightUtils.writePIFile( handler, outputXML.toString() );
			}

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "AdjustTimeContentAdapter: Adapter has IO something wrong." );
		} catch (OperationNotSupportedException | InterruptedException e) {
			logger.log( LogLevel.ERROR, "AdjustTimeContentAdapter: Read / Write PI-XML has something wrong." );
		}
	}
}
