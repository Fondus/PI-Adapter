package tw.fondus.fews.adapter.pi.trigrs;

import java.io.File;

import org.magiclen.magiccommand.Command;
import org.magiclen.magiccommand.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.trigrs.util.RunArguments;

/**
 * Model executable-adapter for running TRIGRS landslide model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TRIGRSExecutable extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args){
		RunArguments arguments = new RunArguments();
		new TRIGRSExecutable().execute(args, arguments);
	}

	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir, File outputDir) throws Exception {	
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		String executeModel = modelArguments.getExecutable();
		String commandString = Strman.append("cmd /c start ", baseDir.getPath(), StringUtils.PATH, executeModel);
			
		Command command = new Command(commandString);
		command.setCommandListener(new CommandListener() {
			@Override
			public void commandStart(String id) {
				log.info("TRIGRS Executable Adapter: Start TRIGRS simulation.");
				piDiagnostics.addMessage(LogLevel.INFO.value(), "TRIGRS Executable Adapter: Start TRIGRS simulation.");
			}

			@Override
			public void commandRunning(String id, String message, boolean isError) {
			}
	
			@Override
			public void commandException(String id, Exception e) {
				log.error("TRIGRS Executable Adapter: when model running has something wrong!", e);
				piDiagnostics.addMessage(LogLevel.ERROR.value(), "TRIGRS Executable Adapter: when model running has something wrong!.");
			}
	
			@Override
			public void commandEnd(String id, int returnValue) {
				log.info("TRIGRS Executable Adapter: TRIGRS simulation end.");
				piDiagnostics.addMessage(LogLevel.INFO.value(), "TRIGRS Executable Adapter: Finished TRIGRS simulation.");
			}
		});
			
		command.run(baseDir);
	}
}
