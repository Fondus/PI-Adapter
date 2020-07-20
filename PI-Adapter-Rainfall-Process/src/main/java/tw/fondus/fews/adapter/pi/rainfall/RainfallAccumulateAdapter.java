package tw.fondus.fews.adapter.pi.rainfall;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.transformation.Aggregations;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.rainfall.argument.IndexArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The rainfall process adapter tool it used to accumulated input PI-XML with specific index range.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class RainfallAccumulateAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		IndexArguments arguments = IndexArguments.instance();
		new RainfallAccumulateAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		IndexArguments modelArguments = this.asArguments( arguments, IndexArguments.class );

		logger.log( LogLevel.INFO, "RainfallAccumulateAdapter: Start the adapter process." );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"RainfallAccumulateAdapter: The input XML file is not exist." );
		try {
			TimeSeriesArrays inputTimeSeriesArrays = TimeSeriesLightUtils.read( inputXML );

			int skip = modelArguments.getSkip();
			int start = modelArguments.getStart();
			int end = modelArguments.getEnd();

			int dataSize = inputTimeSeriesArrays.get( 0 ).size();
			if ( skip + end < dataSize ){
				logger.log( LogLevel.INFO, "RainfallAccumulateAdapter: Try to accumulate the rainfall with skip {}, start {}, end {} and data size {}.", skip, start, end, dataSize );

				long inputTimeStep = inputTimeSeriesArrays.getCommonTimeStepMillis();
				SimpleTimeSeriesContentHandler handler =TimeSeriesLightUtils.seriesHandler();
				TimeSeriesLightUtils.forEach( inputTimeSeriesArrays, array -> {
					TimeSeriesHeader header = array.getHeader();
					String locationId = header.getLocationId();
					String parameter = header.getParameterId();
					String unit = header.getUnit();
					TimeSeriesLightUtils.addHeader( handler, locationId, parameter, unit, inputTimeStep );

					TimeSeriesLightUtils.addValue( handler,
							array.getTime( skip ), Aggregations.accumulative( array, skip + start, skip + end + 1 ) );
				} );

				logger.log( LogLevel.INFO, "RainfallAccumulateAdapter: Write accumulated series to PI-XML." );

				Path outputXML = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
				TimeSeriesLightUtils.write( handler, outputXML );

			} else {
				logger.log( LogLevel.WARN, "RainfallAccumulateAdapter: The skip {} and end {} exceed the data size {}.", skip, end, dataSize );
			}
			logger.log( LogLevel.INFO, "RainfallAccumulateAdapter: Finished the adapter process." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "RainfallAccumulateAdapter: Read the PI-XML has IO something wrong." );
		}
	}
}
