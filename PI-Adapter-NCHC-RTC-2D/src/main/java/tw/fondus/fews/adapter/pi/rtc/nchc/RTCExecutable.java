package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.rtc.nchc.argument.RunArguments;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.FileTools;

/**
 * Model executable-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCExecutable extends PiCommandLineExecute {
	
	public static void main( String[] args ) {
		RunArguments arguments = RunArguments.instance();
		new RTCExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path executableDir = Prevalidated.checkExists( basePath.resolve( modelArguments.getExecutableDir() ),
				"NCHC RTC ExecutableAdapter: Can not find executable directory." );
		
		Path templateDir = Prevalidated.checkExists( basePath.resolve( modelArguments.getTemplateDir() ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find template directory." );
		
		Path DEMDir = Prevalidated.checkExists( templateDir.resolve( "../DEM" ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find DEM directory." );
		
		Path mapStacksPath = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"NCHC RTC ExecutableAdapter: Can not find the XML file of mapstacks." );
		try {
			MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath.toFile(), MapStacks.class );
			String simulationASCFileName = mapStacks.getMapStacks().get( 0 ).getFile().getPattern().getFile();
			
			// Copy template file to executable directory
			logger.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: Copy template file to executable directory." );
			try ( Stream<Path> paths = Files.list( templateDir ) ) {
				paths.forEach( path -> {
					try {
						FileTools.copyFile( templateDir, executableDir, path.getFileName().toString() );
					} catch (IOException e) {
						logger.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Copying template file has something wrong." );
					}
				} );
			}
			Path DEMPath = DEMDir.resolve( modelArguments.getInputs().get( 1 ) );
			if ( PathUtils.isExists( DEMPath ) ) {
				PathUtils.copy( DEMPath, executableDir.resolve( CommonString.INPUT_LOCATION_GRIDS_EST ) );
			} else {
				logger.log( LogLevel.ERROR,
						"NCHC RTC ExecutableAdapter: Copying template DEM file has something wrong." );
			}
			
			logger.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: Start executable adapter of NCHC RTC." );
			// Run model
			String commandGrids = executableDir.resolve( modelArguments.getExecutable().get( 0 ) ).toAbsolutePath().toString();
			String commandRTSIMAP = executableDir.resolve( modelArguments.getExecutable().get( 1 ) ).toAbsolutePath().toString();
			
			IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> { 
				try {
					// Copy preporcess output to executable directory
					String projectName = modelArguments.getProjectName();
					FileTools.copyFile( executableDir, executableDir,
							Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T, String.format( "%03d", timeStep ),
									Strings.UNDERLINE, projectName, FileType.TXT.getExtension() ),
							CommonString.INPUT_VAL_GRIDS_OBS_EST );
					FileTools.copyFile( executableDir, executableDir,
							Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T, String.format( "%03d", timeStep ),
									Strings.UNDERLINE, projectName, FileType.TXT.getExtension() ),
							CommonString.INPUT_LOCATIONS_OBS_GRIDS );
					
					// Run model part1
					Executions.execute( executor -> executor.directory( executableDir.toFile() ),
							commandGrids );
	
					// Backup part1 output
					FileTools.backupOutputFile( executableDir, executableDir, CommonString.OUTPUT_EST_VAL_GRIDS_PARS,
							projectName, timeStep );
					FileTools.backupOutputFile( executableDir, executableDir, CommonString.OUTPUT_EST_VAL_GRIDS_APP,
							projectName, timeStep );
	
					// Change file name for part2 input
					FileTools.copyFile( inputPath, executableDir,
							FileTools.getSimulationASCFileName( simulationASCFileName, timeStep ),
							CommonString.OUTPUT_SIM_WH );
					FileTools.copyFile( executableDir, executableDir,
							CommonString.OUTPUT_EST_VAL_GRIDS_APP, CommonString.OUTPUT_ERR_SIM_WH );
	
					// Run model part2
					Executions.execute( executor -> executor.directory( executableDir.toFile() ),
							commandRTSIMAP );
	
					// Backup part2 output
					FileTools.copyFile( executableDir, executableDir,
							Strman.append( CommonString.OUTPUT_CORR_SIM_WH, FileType.TXT.getExtension() ),
							Strman.append( CommonString.OUTPUT_CORR_SIM_WH, Strings.UNDERLINE, "T1",
									String.format( "%03d", timeStep ), Strings.UNDERLINE, projectName,
									FileType.TXT.getExtension() ) );
	
					String modelOutputASC = Strman.append( CommonString.OUTPUT_CORR_SIM_WH, "_1001",
							FileType.ASC.getExtension() );
					FileTools.copyFile( executableDir, executableDir, modelOutputASC,
							Strman.append( CommonString.OUTPUT_CORR_SIM_WH, "_1001_T1", String.format( "%03d", timeStep ),
									Strings.UNDERLINE, projectName, FileType.ASC.getExtension() ) );
					FileTools.copyFile( executableDir, outputPath, modelOutputASC,
							Strman.append( "Output_ASC0000.", String.format( "%03d", timeStep ) ) );
				} catch (IOException e) {
					e.printStackTrace();
					logger.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Something wrong when copy output file." );
				} catch (InvalidExitValueException | InterruptedException | TimeoutException e) {
					logger.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Execute has something wrong." );
				}
			});
			
			mapStacks.getMapStacks().get( 0 ).getFile().getPattern().setFile( "Output_ASC????.???" );
			XMLUtils.toXML( outputPath.resolve( "run.xml" ).toFile(), mapStacks );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Read folders has something wrong." );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Export map stack has something wrong." );
		}
		logger.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: End executable adapter of NCHC RTC." );
	}
}
