package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.IOException;
import java.nio.file.Path;

import javax.naming.OperationNotSupportedException;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.rtc.nchc.argument.PreAdapterArguments;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.ContentBuilder;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model pre-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreAdapter extends PiCommandLineExecute {
	
	public static void main( String[] args ) {
		PreAdapterArguments arguments = new PreAdapterArguments();
		new RTCPreAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PreAdapterArguments modelArguments = (PreAdapterArguments) arguments;
		
		Path simulationXMLPath = Prevalidated.checkExists( 
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0) ),
				"NCHC RTC PreAdapter: The XML file of simulation is not exist!" );
		
		Path observationXMLPath = Prevalidated.checkExists( 
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(1) ),
				"NCHC RTC PreAdapter: The XML file of observation is not exist!" );
		
		try {
			TimeSeriesArrays similationTimeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( simulationXMLPath );
			TimeSeriesArrays observationTimeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( observationXMLPath );
			
			logger.log( LogLevel.INFO, "NCHC RTC PreAdapter: Start create model input files." );
			
			/** Write model input **/
			this.writeModelInput( logger, similationTimeSeriesArrays,
					observationTimeSeriesArrays, inputPath, modelArguments.getForecast() );
			
			logger.log( LogLevel.INFO, "NCHC RTC PreAdapter: Finished create model input files." );
		} catch (OperationNotSupportedException | IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Read XML has something wrong!" );
		}
	}
	
	/**
	 * Write the model inputs.
	 * 
	 * @param logger
	 * @param similationTimeSeriesArray
	 * @param observationTimeSeriesArray
	 * @param inputPath
	 * @param forecast
	 */
	private void writeModelInput( PiDiagnosticsLogger logger, 
			TimeSeriesArrays similationTimeSeriesArrays,
			TimeSeriesArrays observationTimeSeriesArrays, Path inputPath, int forecast ) {
		try {
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, CommonString.INPUT_CORR_SIM_WH ),
					ContentBuilder.buildInputCorr( similationTimeSeriesArrays.size(), forecast ) );
			FileUtils.writeText( Strman.append( inputPath.toString(), PATH, CommonString.INPUT_WH_EST_OBS_GAUGES ),
					ContentBuilder.buildInputGauges( similationTimeSeriesArrays, observationTimeSeriesArrays ) );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Write model input faild." );
		}
	}
}
