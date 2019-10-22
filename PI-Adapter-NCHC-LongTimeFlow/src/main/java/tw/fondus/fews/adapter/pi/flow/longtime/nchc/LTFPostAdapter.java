package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The model post-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPostAdapter extends PiCommandLineExecute {
	public static final long TEN_DAYS_MILLIS = 10 * 24 * 60 * 60 * 1000;
	public static final long HOURLY_MILLIS = 60 * 60 * 1000;

	public static void main( String[] args ) {
		PiIOArguments arguments = new PiIOArguments();
		new LTFPostAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;

		// Check the XML exists
		Path rainfallXML = Prevalidated.checkExists(
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 0 ) ),
				"NCHC LTF PreAdapter: The XML file of rainfall is not exist." );

		Path waterLevelXML = Prevalidated.checkExists(
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 1 ) ),
				"NCHC LTF PreAdapter: The XML file of water level is not exist." );

		/** Get model output file **/
		Path modelOutput = Prevalidated.checkExists( outputPath.resolve( modelArguments.getInputs().get( 2 ) ),
				"NCHC LTF PostAdapter: The file of model output is not exist." );

		try {
			TimeSeriesArray rainfallTimeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( rainfallXML ).get( 0 );
			TimeSeriesArray waterLevelTimeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( waterLevelXML ).get( 0 );
			DateTime endTime = new DateTime( rainfallTimeSeriesArray.getEndTime() );

			// Read the output content
			List<String> dataList = PathUtils.readAllLines( modelOutput );

			// Create the model PI-XML
			logger.log( LogLevel.INFO, "NCHC NCHC LTF PostAdapter: Start read model output files to PiXML." );

			String[] parameterIds = modelArguments.getParameter().split( StringUtils.COMMA );
			SimpleTimeSeriesContentHandler rainfallHandler = new SimpleTimeSeriesContentHandler();
			TimeSeriesLightUtils.fillPiTimeSeriesHeader( rainfallHandler,
					rainfallTimeSeriesArray.getHeader().getLocationId(),
					parameterIds[0],
					rainfallTimeSeriesArray.getHeader().getUnit() );

			SimpleTimeSeriesContentHandler waterLevelHandler = new SimpleTimeSeriesContentHandler();
			TimeSeriesLightUtils.fillPiTimeSeriesHeader( waterLevelHandler,
					waterLevelTimeSeriesArray.getHeader().getLocationId(),
					parameterIds[1],
					waterLevelTimeSeriesArray.getHeader().getUnit() );
			IntStream.range( 0, dataList.size() ).forEach( tenDays -> {
				String[] split = dataList.get( tenDays ).split( StringUtils.SPACE_MULTIPLE );

				this.fillHourlyValue( tenDays * TEN_DAYS_MILLIS + endTime.getMillis(),
						(tenDays + 1) * TEN_DAYS_MILLIS + endTime.getMillis(), Float.valueOf( split[4] )/24/10,
						rainfallHandler );
				this.fillHourlyValue( tenDays * TEN_DAYS_MILLIS + endTime.getMillis(),
						(tenDays + 1) * TEN_DAYS_MILLIS + endTime.getMillis(), Float.valueOf( split[8] ),
						waterLevelHandler );
			} );

			TimeSeriesLightUtils.writePIFile( rainfallHandler,
					outputPath.resolve( modelArguments.getOutputs().get( 0 ) ).toString() );
			TimeSeriesLightUtils.writePIFile( waterLevelHandler,
					outputPath.resolve( modelArguments.getOutputs().get( 1 ) ).toString() );

			logger.log( LogLevel.INFO, "NCHC NCHC LTF PostAdapter: Finished read model output files to PiXML." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Read model output has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Write PI-XML has something wrong." );
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Read PI-XML has something wrong." );
		}
	}

	/**
	 * Fill model output value by hourly.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param value
	 * @param handler
	 */
	private void fillHourlyValue( long startTime, long endTime, float value, SimpleTimeSeriesContentHandler handler ) {
		long timeLag = endTime - startTime;
		int hours = (int) (timeLag / HOURLY_MILLIS);
		if ( hours > 1 ) {
			IntStream.rangeClosed( 1, hours ).forEach( hour -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, startTime + hour * HOURLY_MILLIS, value );
			} );
		}
	}
}
