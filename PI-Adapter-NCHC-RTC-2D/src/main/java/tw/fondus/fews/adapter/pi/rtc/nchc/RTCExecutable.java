package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.magiclen.magiccommand.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.FileUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.RunArguments;

/**
 * Model executable-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCExecutable extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger( this.getClass() );

	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new RTCExecutable().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		RunArguments modelArguments = (RunArguments) arguments;
		Path executableDir = Paths
				.get( Strman.append( baseDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getExecutableDir() ) );
		Preconditions.checkState( Files.exists( executableDir ),
				"NCHC RTC ExecutableAdapter: Can not find executable directory." );

		Path templateDir = Paths
				.get( Strman.append( baseDir.getPath(), StringUtils.PATH, modelArguments.getTemplateDir() ) );
		Preconditions.checkState( Files.exists( templateDir ),
				"NCHC RTC ExecutableAdapter: Can not find template directory." );

		Path mapStacksPath = Paths
				.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
		Preconditions.checkState( Files.exists( mapStacksPath ),
				"NCHC RTC ExecutableAdapter: Can not find the XML file of mapstacks." );

		MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath.toFile(), MapStacks.class );
		String simulationASCFileName = mapStacks.getMapStacks().get( 0 ).getFile().getPattern().getFile();

		/** Copy template file to executable directory **/
		log.info( "NCHC RTC ExecutableAdapter: Copy template file to executable directory." );
		this.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: Copy template file to executable directory." );
		try (Stream<Path> paths = Files.list( templateDir ) ) {
			String DEMFileName = modelArguments.getInputs().get( 1 );
			paths.forEach( path -> {
				try {
					if ( path.getFileName().toString().equals( DEMFileName ) ) {
						FileUtils.copyFile( templateDir.toFile(), executableDir.toFile(), path.getFileName().toString(),
								CommonString.INPUT_LOCATION_GRIDS_EST );
					}
					FileUtils.copyFile( templateDir.toFile(), executableDir.toFile(), path.getFileName().toString() );
				} catch (IOException e) {
					log.error( "NCHC RTC ExecutableAdapter: Copying template file has something wrong." );
					this.log( LogLevel.ERROR,
							"NCHC RTC ExecutableAdapter: Copying template file has something wrong." );
				}
			} );
		}

		log.info( "NCHC RTC ExecutableAdapter: Start executable adapter of NCHC RTC." );
		this.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: Start executable adapter of NCHC RTC." );

		/** Run model **/
		Command command_GRIDS = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
				modelArguments.getExecutable().get( 0 ) ) );
		Command command_RTSIMAP = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
				modelArguments.getExecutable().get( 1 ) ) );
		
		IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> {
			try {
				/** Copy preporcess output to executable directory **/
				String projectName = modelArguments.getProjectName();
				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(),
						Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T, String.format( "%03d", timeStep ),
								StringUtils.UNDERLINE, projectName, FileType.TXT.getExtension() ),
						CommonString.INPUT_VAL_GRIDS_OBS_EST );
				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(),
						Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T, String.format( "%03d", timeStep ),
								StringUtils.UNDERLINE, projectName, FileType.TXT.getExtension() ),
						CommonString.INPUT_LOCATIONS_OBS_GRIDS );
				

				/** Run model part1 **/
				command_GRIDS.run( executableDir.toFile() );

				/** Backup part1 output **/
				FileUtils.backupOutputFile( executableDir, executableDir, CommonString.OUTPUT_EST_VAL_GRIDS_PARS,
						projectName, timeStep );
				FileUtils.backupOutputFile( executableDir, executableDir, CommonString.OUTPUT_EST_VAL_GRIDS_APP,
						projectName, timeStep );

				/** Change file name for part2 input **/
				FileUtils.copyFile( inputDir, executableDir.toFile(),
						FileUtils.getSimulationASCFileName( simulationASCFileName, timeStep ),
						CommonString.OUTPUT_SIM_WH );
				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(),
						CommonString.OUTPUT_EST_VAL_GRIDS_APP, CommonString.OUTPUT_ERR_SIM_WH );

				/** Run model part2 **/
				command_RTSIMAP.run( executableDir.toFile() );

				/** Backup part2 output **/
				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(),
						Strman.append( CommonString.OUTPUT_CORR_SIM_WH, FileType.TXT.getExtension() ),
						Strman.append( CommonString.OUTPUT_CORR_SIM_WH, StringUtils.UNDERLINE, "T1",
								String.format( "%03d", timeStep ), StringUtils.UNDERLINE, projectName,
								FileType.TXT.getExtension() ) );

				String modelOutputASC = Strman.append( CommonString.OUTPUT_CORR_SIM_WH, "_1001",
						FileType.ASC.getExtension() );
				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(), modelOutputASC,
						Strman.append( CommonString.OUTPUT_CORR_SIM_WH, "_1001_T1", String.format( "%03d", timeStep ),
								StringUtils.UNDERLINE, projectName, FileType.ASC.getExtension() ) );
				FileUtils.copyFile( executableDir.toFile(), outputDir, modelOutputASC,
						Strman.append( "Output_ASC0000.", String.format( "%03d", timeStep ) ) );
			} catch (IOException e) {
				log.error( "NCHC RTC ExecutableAdapter: Something wrong when copy output file." );
				this.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Something wrong when copy output file." );
			}
		} );
		mapStacks.getMapStacks().get( 0 ).getFile().getPattern().setFile( "Output_ASC????.???" );
		XMLUtils.toXML( new File( Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH, "run.xml" ) ), mapStacks );
		log.info( "NCHC RTC ExecutableAdapter: End executable adapter of NCHC RTC." );
		this.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: End executable adapter of NCHC RTC." );
	}
}
