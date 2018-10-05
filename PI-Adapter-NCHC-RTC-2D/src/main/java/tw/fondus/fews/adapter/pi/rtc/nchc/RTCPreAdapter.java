package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.ContentBuilder;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.PreArguments;

/**
 * Model pre-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		PreArguments modelArguments = (PreArguments) arguments;
		try {
			/** Get water level station simulation and observation data. **/
			Path similationXMLPath = Paths
					.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
			Path observationXMLPath = Paths
					.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 1 ) ) );
			if ( !Files.exists( similationXMLPath ) || !Files.exists( observationXMLPath ) ) {
				throw new FileNotFoundException();
			}

			TimeSeriesArray[] similationTimeSeriesArray = TimeSeriesUtils.readPiTimeSeries( similationXMLPath.toFile() )
					.toArray();
			TimeSeriesArray[] observationTimeSeriesArray = TimeSeriesUtils
					.readPiTimeSeries( observationXMLPath.toFile() ).toArray();

			log.info( "NCHC RTC PreAdapter: Start create model input files." );
			this.log( LogLevel.INFO, "NCHC RTC PreAdapter: Start create model input files." );

			/** Write model input **/
			this.writeModelInput( similationTimeSeriesArray, observationTimeSeriesArray, inputDir, modelArguments.getForecast(),
					modelArguments.getOutputs() );

			log.info( "NCHC RTC PreAdapter: Finished create model input files." );
			this.log( LogLevel.INFO, "NCHC RTC PreAdapter: Finished create model input files." );
		} catch (FileNotFoundException e) {
			log.error( "NCHC RTC PreAdapter: Input XML not exist!", e );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Input XML not exist!" );
		} catch (OperationNotSupportedException e) {
			log.error( "NCHC RTC PreAdapter: Read XML not exits!", e );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Read XML not exits!" );
		} catch (IOException e) {
			log.error( "NCHC RTC PreAdapter: IOException!", e );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: IOException!" );
		}
	}

	protected void writeModelInput( TimeSeriesArray[] similationTimeSeriesArray,
			TimeSeriesArray[] observationTimeSeriesArray, File inputDir, int forecast, List<String> outputs ) {
		try {
			FileUtils.writeText( Strman.append( inputDir.getPath(), StringUtils.PATH, outputs.get( 0 ) ),
					ContentBuilder.buildInputCorr( similationTimeSeriesArray.length, forecast ) );
			FileUtils.writeText( Strman.append( inputDir.getPath(), StringUtils.PATH, outputs.get( 1 ) ),
					ContentBuilder.buildInputGauges( similationTimeSeriesArray, observationTimeSeriesArray ) );
		} catch (IOException e) {
			log.error( "NCHC RTC PreAdapter: Write model input faild." );
			this.log( LogLevel.ERROR, "NCHC RTC PreAdapter: Write model input faild." );
		}

	}

}
