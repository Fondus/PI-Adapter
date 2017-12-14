package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;

/**
 * Parent class with Model post-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffPostAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File logDir, File logFile, File inputDir, File outputDir){
		PiArguments modelArguments = (PiArguments) arguments;
		
		try {
			File timeFile = new File( Strman.append(inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0)) );
			Preconditions.checkState(timeFile.exists(), "NCHC RainRunoff PostAdapter: The temporary Time Meta information file not exist.");
			
			/** Read Time meta information file **/
			Optional<String> timeInfos = this.readTimeMetaFile(timeFile);
			if (timeInfos.isPresent()){
				String[] temps = timeInfos.get().split(StringUtils.SPACE_MULTIPLE);
				
				long startTimeMillis = Long.valueOf(temps[0]);
				long timeStepMillis = Long.valueOf(temps[1]);
				
				log.info("NCHC RainRunoff PostAdapter: Start read model output files to PiXML.");
				piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff PostAdapter: Start read model output files to PiXML.");
				
				/** Read model output and create XML content **/
				SimpleTimeSeriesContentHandler contentHandler = new SimpleTimeSeriesContentHandler();
				
				File[] modelOutputs = outputDir.listFiles(FileUtils.TXT_FILE_FILTER);
				Stream.of(modelOutputs).forEach(modelOutput -> {
					try {
						
						this.parseFileContent(modelOutput, contentHandler,
								modelArguments.getParameter(), modelArguments.getUnit(),
								startTimeMillis, timeStepMillis);
						
					} catch (IOException e) {
						log.info("NCHC RainRunoff PostAdapter: Read model output file has something wrong.");
						piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PostAdapter: Read model output has something wrong.");
					}
				});
				
				TimeSeriesUtils.writePIFile(contentHandler, Strman.append(outputDir.getPath(), StringUtils.PATH, modelArguments.getOutputs().get(0)));
				
				log.info("NCHC RainRunoff PostAdapter: Finished read model output files to PiXML.");
				piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff PostAdapter: Finished read model output files to PiXML.");
				
			} else {
				log.warn("NCHC RainRunoff PostAdapter: The temporary Time Meta information file has not content.");
				piDiagnostics.addMessage(LogLevel.WARN.value(), "NCHC RainRunoff PostAdapter: The temporary Time Meta information file has not content!");
			}
			
		} catch (IOException e) {
			log.error("NCHC RainRunoff PostAdapter: IOException!", e);
			piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PostAdapter: IOException!");
		} catch (InterruptedException e) {
			log.error("NCHC RainRunoff PostAdapter: Write XML has something wrong!", e);
			piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff PostAdapter: Write XML has something wrongs!");
		}
	}
	
	/**
	 * Read temporary Time Meta information file.
	 * 
	 * @param timeFile
	 * @return
	 * @throws IOException
	 */
	protected Optional<String> readTimeMetaFile(File timeFile) throws IOException{
		List<String> fileLines = Files.readAllLines(Paths.get(timeFile.getPath()), StandardCharsets.UTF_8);
		
		return Optional.ofNullable(fileLines.get(0));
	}
	
	/**
	 * Read model output content -> SimpleTimeSeriesContentHandler logic.
	 * 
	 * @param outputFile		: model output.
	 * @param contentHandler	: fews pi xml pojo for write.
	 * @param parameter			: model parameter.
	 * @param unit				: model parameter unit.
	 * @param startTimeMillis	: start time with millisecond.
	 * @param timeStepMillis	: time step with millisecond.
	 * @throws IOException		: 
	 */
	protected abstract void parseFileContent(File outputFile,
			SimpleTimeSeriesContentHandler contentHandler, String parameter, String unit,
			long startTimeMillis, long timeStepMillis) throws IOException;
}
