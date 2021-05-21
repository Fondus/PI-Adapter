package tw.fondus.fews.adapter.pi.rtc.nchc;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.rtc.nchc.argument.PreAdapterArguments;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.ContentBuilder;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Model pre-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class RTCPreAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PreAdapterArguments arguments = PreAdapterArguments.instance();
		new RTCPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PreAdapterArguments modelArguments = (PreAdapterArguments) arguments;
		
		Path simulationXMLPath = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"NCHC RTC PreAdapter: The XML file of simulation is not exist!" );
		
		Path observationXMLPath = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 1 ) ),
				"NCHC RTC PreAdapter: The XML file of observation is not exist!" );
		
		try {
			TimeSeriesArrays simulationTimeSeriesArrays = TimeSeriesLightUtils.read( simulationXMLPath );
			TimeSeriesArrays observationTimeSeriesArrays = TimeSeriesLightUtils.read( observationXMLPath );
			
			logger.log( LogLevel.INFO, "NCHC RTC PreAdapter: Start create model input files." );
			
			// Write model input
			this.writeModelInput( simulationTimeSeriesArrays,
					observationTimeSeriesArrays, inputPath, modelArguments.getForecast() );
			
			logger.log( LogLevel.INFO, "NCHC RTC PreAdapter: Finished create model input files." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Read XML has something wrong!" );
		}
	}
	
	/**
	 * Write the model inputs.
	 *
	 * @param simulationTimeSeriesArrays
	 * @param observationTimeSeriesArrays
	 * @param inputPath
	 * @param forecast
	 */
	private void writeModelInput( TimeSeriesArrays simulationTimeSeriesArrays,
			TimeSeriesArrays observationTimeSeriesArrays, Path inputPath, int forecast ) {
		PathWriter.write( inputPath.resolve( CommonString.INPUT_CORR_SIM_WH ),
				ContentBuilder.buildInputCorr( simulationTimeSeriesArrays.size(), forecast ) );
		PathWriter.write( inputPath.resolve( CommonString.INPUT_WH_EST_OBS_GAUGES ),
				ContentBuilder.buildInputGauges( simulationTimeSeriesArrays, observationTimeSeriesArrays ) );
	}
}
