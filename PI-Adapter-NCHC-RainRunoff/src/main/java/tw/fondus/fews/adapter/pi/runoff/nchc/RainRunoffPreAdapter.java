package tw.fondus.fews.adapter.pi.runoff.nchc;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeStep;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parent class with Model pre-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public abstract class RainRunoffPreAdapter extends PiCommandLineExecute {
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = this.asIOArguments( arguments );
		try {
			Path inputXML = Prevalidated.checkExists( 
					inputPath.resolve( modelArguments.getInputs().get(0) ),
					"NCHC RainRunoffPreAdapter: The input XML not exists!" );
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			logger.log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Start create model input files.");
			
			// Create model input
			this.writeModelInput( timeSeriesArrays, inputPath, modelArguments.getOutputs().get(0) );
			
			logger.log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Finished create model input files.");
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RainRunoff PreAdapter: No time series found in file in the model input files!" );
		}
	}
	
	/**
	 * Write the RR model input file with model input folder and name.
	 *
	 * @param timeSeriesArrays time series arrays
	 * @param inputPath input path
	 * @param fileName file name
	 */
	private void writeModelInput( TimeSeriesArrays timeSeriesArrays,
			Path inputPath, String fileName ) {
		this.writeTimeMetaInfo( inputPath.resolve( fileName ), timeSeriesArrays.get( 0 ).getTime(0), timeSeriesArrays.get( 0 ).getTimeStep() );

		TimeSeriesLightUtils.forEach( timeSeriesArrays, array -> {
			String modelInput = this.createModelInputContent( array );
			String locationId = array.getHeader().getLocationId();
			this.getLogger().log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Create the model input with location id: {}.", locationId );
			PathWriter.write( inputPath.resolve( locationId + FileType.TXT.getExtension() ), modelInput );
		} );
	}

	/**
	 * Write the temporary time meta-information file to model input folder.
	 *
	 * @param metaInfoPath time meta info file
	 * @param time time
	 * @param timeStep time step
	 */
	private void writeTimeMetaInfo( Path metaInfoPath, long time, TimeStep timeStep ) {
		this.getLogger().log( LogLevel.INFO, "NCHC RainRunoff PreAdapter: Create the time-meta information with file: {}.",
				PathUtils.getNameWithoutExtension( metaInfoPath ) );
		PathWriter.write( metaInfoPath, Strman.append( String.valueOf( time ), Strings.SPACE, String.valueOf( timeStep.getStepMillis() ) ) );
	}
	
	/**
	 * Mapping timeSeriesArray -> rain-runoff model input content logic.
	 * 
	 * @param array time series array
	 * @return rain-runoff model input content
	 */
	protected abstract String createModelInputContent( TimeSeriesArray array );
}
