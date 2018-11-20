package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.magiclen.magiccommand.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
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

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		RunArguments modelArguments = (RunArguments) arguments;
		Path executableDir = Paths
				.get( Strman.append( baseDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getExecutableDir() ) );
		Path templateDir = Paths
				.get( Strman.append( baseDir.getPath(), StringUtils.PATH, modelArguments.getTempDir() ) );
		if ( Files.exists( executableDir ) && Files.exists( templateDir ) ) {
			/** Get all input, output and executable **/
			String tempInputGridObs = modelArguments.getInputs().get( 0 );
			String inputGridObs = modelArguments.getInputs().get( 1 );
			String inputSimASC = modelArguments.getInputs().get( 2 );
			String inputERRSimASC = modelArguments.getInputs().get( 3 );
			Path mapStacksPath = Paths
					.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 4 ) ) );
			MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath.toFile(), MapStacks.class );
			String simulationASCFileName = mapStacks.getMapStacks().get( 0 ).getFile().getPattern().getFile();

			String output_PARS = modelArguments.getOutputs().get( 0 );
			String output_APP = modelArguments.getOutputs().get( 1 );
			String output_WH_txt = modelArguments.getOutputs().get( 2 );
			String output_WH_asc = modelArguments.getOutputs().get( 3 );

			Command command_GRIDS = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
					modelArguments.getExecutable().get( 0 ) ) );
			Command command_RTSIMAP = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
					modelArguments.getExecutable().get( 1 ) ) );

			log.info( "NCHC RTC ExecutableAdapter: Start executable adapter of NCHC RTC." );
			this.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: Start executable adapter of NCHC RTC." );
			/** Run model **/
			IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> {
				try {
					/** Copy preporcess output to executable directory **/
					String projectName = modelArguments.getProjectName();
					
					FileUtils.copyFile( templateDir.toFile(),
							executableDir.toFile(), Strman.append( tempInputGridObs, String.format( "%03d", timeStep ),
									StringUtils.UNDERLINE, projectName, StringUtils.DOT, FileType.TXT.getType() ),
							inputGridObs );
					
					/** Run model part1 **/
					command_GRIDS.run( executableDir.toFile() );

					/** Backup part1 output **/
					FileUtils.backupOutputFile( executableDir, templateDir, output_PARS, projectName, timeStep );
					FileUtils.backupOutputFile( executableDir, templateDir, output_APP, projectName, timeStep );

					/** Change file name for part2 input **/
					FileUtils.copyFile( inputDir, executableDir.toFile(),
							FileUtils.getSimulationASCFileName( simulationASCFileName, timeStep ), inputSimASC );
					FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(), output_APP, inputERRSimASC );
					
					/** Run model part2 **/
					command_RTSIMAP.run( executableDir.toFile() );

					/** Backup part2 output **/
					FileUtils.backupOutputFile( executableDir, outputDir.toPath(), output_WH_txt, projectName,
							timeStep );
					FileUtils.backupOutputFile( executableDir, outputDir.toPath(), output_WH_asc, projectName, timeStep,
							FileType.ASC );
				} catch (IOException e) {
					log.error( "NCHC RTC ExecutableAdapter: Something worng when copy output file." );
					this.log( LogLevel.ERROR, "NCHC RTC ExecutableAdapter: Something worng when copy output file." );
				}
			} );

			log.info( "NCHC RTC ExecutableAdapter: End executable adapter of NCHC RTC." );
			this.log( LogLevel.INFO, "NCHC RTC ExecutableAdapter: End executable adapter of NCHC RTC." );
		} else {
			log.error( "NCHC RTC ExecutableAdapter: The model executable or template directory not exist." );
			this.log( LogLevel.ERROR,
					"NCHC RTC ExecutableAdapter: The model executable or template directory not exist." );
		}
	}
}
