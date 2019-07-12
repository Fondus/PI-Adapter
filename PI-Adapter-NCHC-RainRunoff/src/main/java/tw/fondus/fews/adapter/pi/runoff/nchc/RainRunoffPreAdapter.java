package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.naming.OperationNotSupportedException;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeStep;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Parent class with Model pre-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffPreAdapter extends PiCommandLineExecute {
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = (PiIOArguments) arguments;
		try {
			String inputXMLPath = Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0));
			Path inputXML = Paths.get( inputXMLPath );
			Prevalidated.checkExists( inputXML, "NCHC RainRunoffPreAdapter: The input XML not exists!" );
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			logger.log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Start create model input files.");
			
			// Create model input
			this.writeModelInput( logger, timeSeriesArrays, inputPath, modelArguments.getOutputs().get(0) );
			
			logger.log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Finished create model input files.");
			
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "NCHC RainRunoff PreAdapter: Read XML not exists or content empty!");
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RainRunoff PreAdapter: Read XML or write the time meta-information has something faild!" );
		}
	}
	
	/**
	 * Write the RR model input file with model input folder and name.
	 * 
	 * @param logger
	 * @param timeSeriesArrays
	 * @param inputPath
	 * @param fileName
	 * @throws IOException 
	 */
	private void writeModelInput( PiDiagnosticsLogger logger, TimeSeriesArrays timeSeriesArrays,
			Path inputPath, String fileName ) throws IOException {
		String modelInputPath = Strman.append( inputPath.toString(), PATH );
		this.writeTimeMetaInfo( modelInputPath, fileName, timeSeriesArrays.get( 0 ).getTime(0), timeSeriesArrays.get( 0 ).getTimeStep() );
		
		Stream.of( timeSeriesArrays.toArray() ).forEach( array -> {
			String modelInput = this.createModelInputContent( array );
			
			try {
				FileUtils.writeText( Strman.append( modelInputPath, array.getHeader().getLocationId(), FileType.TXT.getExtension() ), modelInput );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "NCHC RainRunoff PreAdapter: Write model input faild!" );
			}
		} );
	}
	
	/**
	 * Write the temporary time meta-information file to model input folder.
	 * 
	 * @param modelInputFolderPath
	 * @param fileName
	 * @param time
	 * @param timeStep
	 * @throws IOException
	 */
	private void writeTimeMetaInfo( String modelInputPath, String fileName, long time, TimeStep timeStep ) throws IOException {
		String timeMetaInfo = Strman.append( modelInputPath, fileName );
		FileUtils.writeText( timeMetaInfo, Strman.append( String.valueOf( time ), StringUtils.SPACE_WHITE, String.valueOf( timeStep.getStepMillis() ) ) );
	}
	
	/**
	 * Mapping timeSeriesArray -> rain-runoff model input content logic.
	 * 
	 * @param array
	 * @return
	 */
	protected abstract String createModelInputContent( TimeSeriesArray array );
}
