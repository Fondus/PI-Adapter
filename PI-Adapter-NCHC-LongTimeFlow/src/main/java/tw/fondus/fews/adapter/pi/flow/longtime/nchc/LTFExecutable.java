package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.zeroturnaround.exec.InvalidExitValueException;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.flow.longtime.nchc.util.RunArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * The model executable for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFExecutable extends PiCommandLineExecute {
	private static final String[] OUTPUTS = {
		"OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT",
		"SUMMARY_RESULTS_EST_FLOW_ANN_GA-SA_MTF.TXT",
		"OUTPUT_EST_RAIN_ANN_GA-SA_MTF.TXT",
		"OUTPUT_EST_RAIN_FLOW_ANN_GA-SA_MTF.TXT"
	};
	
	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new LTFExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path executablePath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getExecutableDir()) );
		Prevalidated.checkExists( executablePath, "NCHC LTF Executable: The model executable directory not exist." );
		
		Path templatePath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getTemplateDir() ) );
		Prevalidated.checkExists( templatePath, "NCHC LTF Executable: The template directory is not exist." );
		
		logger.log( LogLevel.INFO, "NCHC LTF Executable: Start the executable process." );
		
		try {
			/** Copy model input file to executable directory **/
			FileUtils.copy( Strman.append( inputPath.toString(), PATH, "DATA_INP_RAIN.TXT" ),
					Strman.append( executablePath.toString(), PATH, "DATA_INP_RAIN.TXT" ) );
			FileUtils.copy( Strman.append( inputPath.toString(), PATH, "DATA_INP_FLOW.TXT" ),
					Strman.append( executablePath.toString(), PATH, "DATA_INP_FLOW.TXT" ) );
			
			/** Copy model to executable directory from template directory **/
			FileUtils.copy( templatePath.toFile().listFiles(), executablePath.toFile() );
			
			Path executable = Paths.get( Strman.append( executablePath.toString(), PATH, modelArguments.getExecutable().get( 0 ) ) );
			Prevalidated.checkExists( executable, "NCHC LTF Executable: The executable is not exist." );
			
			/** Run model **/
			Executions.execute( executor -> executor.directory( executablePath.toFile() ),
					executable.toString() );
			
			/** Backup model output file by project name **/
			this.backupOutputFile( logger, executablePath, modelArguments.getProjectName() );
			
			/** Move model output to output directory **/
			FileUtils.copy( Strman.append( executablePath.toString(), PATH, OUTPUTS[0] ), 
					Strman.append( outputPath.toString(), PATH, OUTPUTS[0] ));
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF Executable: has IO problem." );
		} catch (InvalidExitValueException | InterruptedException | TimeoutException e) {
			logger.log( LogLevel.ERROR, "NCHC LTF Executable: executable has something problem.");
		} 
		
		logger.log( LogLevel.INFO, "NCHC LTF Executable: Finished the executable process." );
	}
	
	/**
	 * Backup the model outputs.
	 * 
	 * @param logger
	 * @param basePath
	 * @param projectName
	 */
	private void backupOutputFile( PiDiagnosticsLogger logger, Path basePath, String projectName ) {
		Stream.of( OUTPUTS ).forEach( output -> {
			String fileName = FileUtils.getNameWithoutExt( output );
			String backupFileName = Strman.append( fileName, StringUtils.UNDERLINE, projectName, FileType.TXT.getExtension() );
			try {
				logger.log( LogLevel.INFO, "NCHC LTF Executable: Backup output file: {} to {}.", output, backupFileName );
				FileUtils.copy( Strman.append( basePath.toString(), PATH, output ), Strman.append( basePath.toString(), PATH, backupFileName ) );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "NCHC LTF Executable: Backup output file: {} has something wrong.", output );
			}
		} );
	}
}
