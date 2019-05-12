package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.ContentBuilder;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.PreAdapterArguments;

/**
 * Model pre-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	
	
	public static void main( String[] args ) {
		PreAdapterArguments arguments = new PreAdapterArguments();
		new RTCPreAdapter().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		PreAdapterArguments modelArguments = (PreAdapterArguments) arguments;
		try {
			/** Get water level station simulation and observation data. **/
			Path similationXMLPath = Paths
					.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
			Preconditions.checkState( Files.exists( similationXMLPath ), "NCHC RTC PreAdapter: The XML file of simulation is not exist." );
			
			Path observationXMLPath = Paths
					.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 1 ) ) );
			Preconditions.checkState( Files.exists( observationXMLPath ), "NCHC RTC PreAdapter: The XML file of observation is not exist." );
			
			TimeSeriesArray[] similationTimeSeriesArray = TimeSeriesUtils.readPiTimeSeries( similationXMLPath.toFile() )
					.toArray();
			TimeSeriesArray[] observationTimeSeriesArray = TimeSeriesUtils
					.readPiTimeSeries( observationXMLPath.toFile() ).toArray();

			log.info( "NCHC RTC PreAdapter: Start create model input files." );
			this.log( LogLevel.INFO, "NCHC RTC PreAdapter: Start create model input files." );

			/** Write model input **/
			this.writeModelInput( similationTimeSeriesArray, observationTimeSeriesArray, inputDir, modelArguments.getForecast() );

			log.info( "NCHC RTC PreAdapter: Finished create model input files." );
			this.log( LogLevel.INFO, "NCHC RTC PreAdapter: Finished create model input files." );
		} catch (OperationNotSupportedException e) {
			log.error( "NCHC RTC PreAdapter: Read XML not exits!", e );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Read XML not exits!" );
		} catch (IOException e) {
			log.error( "NCHC RTC PreAdapter: IOException!", e );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: IOException!" );
		}
	}

	protected void writeModelInput( TimeSeriesArray[] similationTimeSeriesArray,
			TimeSeriesArray[] observationTimeSeriesArray, File inputDir, int forecast ) {
		try {
			FileUtils.writeText( Strman.append( inputDir.getPath(), StringUtils.PATH, CommonString.INPUT_CORR_SIM_WH ),
					ContentBuilder.buildInputCorr( similationTimeSeriesArray.length, forecast ) );
			FileUtils.writeText( Strman.append( inputDir.getPath(), StringUtils.PATH, CommonString.INPUT_WH_EST_OBS_GAUGES ),
					ContentBuilder.buildInputGauges( similationTimeSeriesArray, observationTimeSeriesArray ) );
		} catch (IOException e) {
			log.error( "NCHC RTC PreAdapter: Write model input faild." );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Write model input faild." );
		}

	}

}
