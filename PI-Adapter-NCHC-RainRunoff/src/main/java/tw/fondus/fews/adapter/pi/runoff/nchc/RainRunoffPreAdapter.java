package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeStep;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;

/**
 * Parent class with Model pre-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffPreAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File logDir, File logFile, File inputDir, File outputDir){
		PiArguments modelArguments = (PiArguments) arguments;
		
		try {
			String inputXMLPath = Strman.append(inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0));
			File inputXML = new File(inputXMLPath);
			if ( !inputXML.exists() ){
				throw new FileNotFoundException();
			}
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries(inputXML);
			
			log.info("NCHC RainRunoff PreAdapter: Start create model input files.");
			piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff PreAdapter: Start create model input files.");
			
			this.writeModelInput(piDiagnostics, timeSeriesArrays, inputDir, modelArguments.getOutputs().get(0));
			
			log.info("NCHC RainRunoff PreAdapter: Finished create model input files.");
			
		} catch (FileNotFoundException e) {
			log.error("NCHC RainRunoff PreAdapter: Input XML not exits!", e);
			piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PreAdapter: Input XML not exits!");
		} catch (OperationNotSupportedException e) {
			log.error("NCHC RainRunoff PreAdapter: Read XML not exits!", e);
			piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PreAdapter: Read XML not exits!");
		} catch (IOException e) {
			log.error("NCHC RainRunoff PreAdapter: IOException!", e);
			piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PreAdapter: IOException!");
		}
	}
	
	/**
	 * Write RR model input.
	 * 
	 * @param piDiagnostics
	 * @param timeSeriesArrays
	 * @param baseDir
	 * @param outputFile
	 * @throws IOException
	 */
	protected void writeModelInput(PiDiagnostics piDiagnostics, TimeSeriesArrays timeSeriesArrays, File baseDir, String outputFile) throws IOException{
		TimeSeriesArray[] timeSeriesArray = timeSeriesArrays.toArray();
		
		String outputPath = Strman.append(baseDir.getPath(), StringUtils.PATH);
		this.writeTimeMetaFile(outputPath, outputFile, timeSeriesArray[0].getTime(0), timeSeriesArray[0].getTimeStep());
		
		Stream.of(timeSeriesArray).forEach(array -> {
			String modelInput = this.createFileContent(array);
			
			try {
				FileUtils.writeText( Strman.append(outputPath, array.getHeader().getLocationId(), ".txt") , modelInput);
			} catch (IOException e) {
				piDiagnostics.addMessage(LogLevel.ERROR.value(), "Write model input faild.");
			}
		});
	}
	
	/**
	 * Mapping timeSeriesArray -> rain-runoff model input content logic.
	 * 
	 * @param array
	 * @return
	 */
	protected abstract String createFileContent(TimeSeriesArray array);
	
	/**
	 * Write to temporary Time Meta information file.
	 * 
	 * @param outputPath
	 * @param outputFile
	 * @param time
	 * @param timeStep
	 * @throws IOException
	 */
	protected void writeTimeMetaFile(String outputPath, String outputFile, long time, TimeStep timeStep) throws IOException {
		String targetFile = Strman.append(outputPath, outputFile);
		
		FileUtils.writeText(targetFile, Strman.append( String.valueOf(time), " ", String.valueOf(timeStep.getStepMillis())));
	}
}
