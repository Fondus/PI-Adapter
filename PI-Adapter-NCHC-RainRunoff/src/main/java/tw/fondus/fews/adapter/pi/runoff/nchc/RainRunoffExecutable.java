package tw.fondus.fews.adapter.pi.runoff.nchc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.magiclen.magiccommand.Command;
import org.magiclen.magiccommand.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Parent class with Model executable-adapter for running NCHC RR model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public abstract class RainRunoffExecutable extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected final Map<String, String> parametersMap = new HashMap<String, String>();
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File logDir, File logFile, File inputDir, File outputDir){
		RunArguments modelArguments = (RunArguments) arguments;
		
		File executableDir = new File( Strman.append(baseDir.getPath(), StringUtils.PATH, modelArguments.getExecutablePath()) );
		Preconditions.checkState(executableDir.exists(), "NCHC RainRunoff ExecutableAdapter: The model executable directory not exist.");
		
		File parameterDir = new File( Strman.append(baseDir.getPath(), StringUtils.PATH, modelArguments.getParametersPath()) );
		Preconditions.checkState(parameterDir.exists(), "NCHC RainRunoff ExecutableAdapter: The model parameters directory not exist.");
		
		String executableDirPath = Strman.append(executableDir.getPath(), StringUtils.PATH);
		String executableInput =  Strman.append(executableDirPath, modelArguments.getInputs().get(0));
		String parameterInput = Strman.append(executableDirPath, modelArguments.getInputs().get(1));
		String modelOutput = Strman.append(executableDirPath, modelArguments.getOutputs().get(0));
		String parameterPrefix = Strman.append(FileUtils.getNameWithoutExt(parameterInput), "_");
		
		/** Get model parameters use recursive **/
		this.readParameters(parameterDir, parameterPrefix);
		
		/** Create executble command **/
		String executable = modelArguments.getExecutable();
		Command command = new Command( Strman.append( executableDir.getPath(), StringUtils.PATH, executable));
		command.setCommandListener(new CommandListener() {
			@Override
			public void commandStart(String id) {
				log.info("NCHC RainRunoff ExecutableAdapter: Start {} simulation.", executable );
				piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff ExecutableAdapter: Start RainRunoff simulation.");
			}

			@Override
			public void commandRunning(String id, String message, boolean isError) {
				
			}

			@Override
			public void commandException(String id, Exception e) {
				log.error("NCHC RainRunoff ExecutableAdapter: when {} running has something wrong!", executable, e);
				piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff ExecutableAdapter: when model running has something wrong!.");
			}

			@Override
			public void commandEnd(String id, int returnValue) {
				log.info("NCHC RainRunoff ExecutableAdapter: {} simulation end.", executable);
				piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff ExecutableAdapter: Finished RainRunoff simulation.");
			}
		});
		
		/** Run loop **/
		File[] modelInputs = inputDir.listFiles(FileUtils.TXT_FILE_FILTER);
		Stream.of(modelInputs).forEach(modelInput -> {
			String locationId = FileUtils.getNameWithoutExt(modelInput);
			
			try {
				if ( this.parametersMap.containsKey(locationId)){
					/** Move model input to executable directory **/
					log.info("NCHC RainRunoff ExecutableAdapter: Copy model input to executable directory. Processing {} now.", locationId);
					piDiagnostics.addMessage(LogLevel.INFO.value(),
							Strman.append("NCHC RainRunoff ExecutableAdapter: Copy model input to executable directory.", "Processing ", locationId, " now."));
					
					FileUtils.move(modelInput.getPath(), executableInput);
					
					log.info("NCHC RainRunoff ExecutableAdapter: Copy input parameter file to executable directory.");
					piDiagnostics.addMessage(LogLevel.INFO.value(), "NCHC RainRunoff ExecutableAdapter: Copy input parameter file to executable directory.");
					
					FileUtils.copy( this.parametersMap.get(locationId), parameterInput);
					
					/** Run model **/
					command.run(executableDir);
					
					/** Move mode output to output directory **/
					FileUtils.move(modelOutput, 
							Strman.append(outputDir.getPath(), StringUtils.PATH, modelInput.getName()));
				} else {
					log.warn("NCHC RainRunoff ExecutableAdapter: Parameter file directory not contain {}.", locationId);
					piDiagnostics.addMessage(LogLevel.WARN.value(),
							Strman.append("NCHC RainRunoff ExecutableAdapter: Parameter file directory not contain ", locationId, " ."));
				}
				
			} catch (IOException e) {
				log.info("NCHC RainRunoff ExecutableAdapter: Copy file has something wrong.");
				piDiagnostics.addMessage(LogLevel.ERROR.value(), "NCHC RainRunoff ExecutableAdapter: Copy file has something wrong.");
			}
		});
	}
	
	/**
	 * Read all parameter files to parametersMap.
	 * 
	 * @param baseDir
	 * @param prefix
	 * @throws FileNotFoundException
	 */
	protected void readParameters(File baseDir, String prefix){
		if (baseDir.exists() && baseDir.isDirectory()) {
			File[] subDirs = baseDir.listFiles();
			Stream.of(subDirs).forEach(subDir -> {
				if (subDir.isDirectory()) {
					this.readParameters(subDir, prefix);
				} else {
					this.addParameterFiles(subDir, prefix);
				}
			});
		}
	}
	
	/**
	 * The logic of parse model parameter files to parametersMap.
	 * 
	 * @param parameterFile
	 * @param prefix
	 */
	protected abstract void addParameterFiles(File parameterFile, String prefix);
}
