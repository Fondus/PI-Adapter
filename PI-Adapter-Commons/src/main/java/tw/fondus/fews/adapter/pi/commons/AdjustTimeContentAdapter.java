package tw.fondus.fews.adapter.pi.commons;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.extend.AdjustTimeArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;

/**
 * The commons adapter tool it used to adjust time of input PI-XML by base PI-XML start time.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class AdjustTimeContentAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		AdjustTimeArguments arguments = AdjustTimeArguments.instance();
		new AdjustTimeContentAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		AdjustTimeArguments modelArguments = this.asArguments( arguments, AdjustTimeArguments.class );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"AdjustTimeContentAdapter: The input XML file is not exist." );
		try {
			TimeSeriesArrays inputTimeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			long inputTimeStep = inputTimeSeriesArrays.getCommonTimeStepMillis();

			if ( modelArguments.getMode() == 0 ){
				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: Prepare the adjust time mode by base XML." );

				Path baseXML = Prevalidated.checkExists(
						inputPath.resolve( modelArguments.getInputs().get( 1 ) ),
						"AdjustTimeContentAdapter: The base XML file is not exist." );

				TimeSeriesArrays baseTimeSeriesArrays = TimeSeriesLightUtils.read( baseXML );
				long baseTimeStep = baseTimeSeriesArrays.getCommonTimeStepMillis();
				DateTime startTime = new DateTime( baseTimeSeriesArrays.getPeriod().getStartTime() );
				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: The base XML as start time is {}.", startTime.toString() );
				if ( inputTimeStep != baseTimeStep ){
					logger.log( LogLevel.WARN, "AdjustTimeContentAdapter: The input time step and base time step not equal, adapter will not do anything." );
				} else {
					this.adjustTimeContent( modelArguments, outputPath, inputTimeSeriesArrays, startTime );
				}
			} else {
				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: Prepare the adjust time mode by time zero." );

				DateTime startTime = modelArguments.getTimeZero();
				logger.log( LogLevel.INFO, "AdjustTimeContentAdapter: The time zero as start time is {}.", startTime.toString() );

				this.adjustTimeContent( modelArguments, outputPath, inputTimeSeriesArrays, startTime );
			}

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "AdjustTimeContentAdapter: Adapter has IO something wrong." );
		}
	}

	/**
	 * The process used to adjust time series arrays and write to XML.
	 *
	 * @param modelArguments adapter arguments
	 * @param outputPath output path
	 * @param timeSeriesArrays time series arrays will be adjust
	 * @param startTime start time
	 * @throws IOException has IO Exception
	 * @since 3.0.0
	 */
	private void adjustTimeContent( AdjustTimeArguments modelArguments, Path outputPath,
			TimeSeriesArrays timeSeriesArrays, DateTime startTime ) throws IOException {
		this.getLogger().log( LogLevel.INFO, "AdjustTimeContentAdapter: Start the adjust the time." );

		long inputTimeStep = timeSeriesArrays.getCommonTimeStepMillis();
		this.getLogger().log( LogLevel.INFO, "AdjustTimeContentAdapter: The input time step millis: {}.", inputTimeStep );

		SimpleTimeSeriesContentHandler handler = TimeSeriesLightUtils.seriesHandler();
		TimeSeriesLightUtils.forEach( timeSeriesArrays, array -> {
			TimeSeriesHeader header = array.getHeader();
			String locationId = header.getLocationId();
			String parameter = header.getParameterId();
			String unit = header.getUnit();

			if ( inputTimeStep == 86400000L ){
				TimeSeriesLightUtils.addHeaderIrregular( handler, locationId, parameter, unit );
			} else {
				TimeSeriesLightUtils.addHeader( handler, locationId, parameter, unit, inputTimeStep );
			}

			IntStream.range( 0, array.size() ).forEach( i ->
					TimeSeriesLightUtils.addValue( handler,
							startTime.plus( i * inputTimeStep ).getMillis(), TimeSeriesLightUtils.getValue( array, i ) )
			);
		} );

		this.getLogger().log( LogLevel.INFO, "AdjustTimeContentAdapter: Write the adjust PI-XML." );
		Path outputXML = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
		TimeSeriesLightUtils.write( handler, outputXML );
	}
}
