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
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.FileUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.RunArguments;

/**
 * Model preprocess executable-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreprocessExecutable extends PiCommandLineExecute {
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
			log.info( "NCHC RTC Preprocess ExecutableAdapter: Start executable adapter of NCHC RTC preprocess." );
			this.log( LogLevel.INFO,
					"NCHC RTC Preprocess ExecutableAdapter: Start executable adapter of NCHC RTC preprocess." );

			/** Copy model input file to executable directory from input directory **/
			modelArguments.getInputs().forEach( input -> {
				try {
					FileUtils.copyFile( inputDir, executableDir.toFile(), input );
				} catch (IOException e) {
					log.error( "NCHC RTC Preprocess ExecutableAdapter: Something worng when copy input file.", e );
					this.log( LogLevel.ERROR,
							"NCHC RTC Preprocess ExecutableAdapter: Something worng when copy input file." );
				}
			} );

			/** Run model preprocess **/
			Command command = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
					modelArguments.getExecutable().get( 0 ) ) );
			command.run( executableDir.toFile() );

			/** Backup data of model preprocess output **/
			IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> {
				try {
					String fileName = Strman.append( modelArguments.getOutputs().get( 0 ),
							String.format( "%03d", timeStep ), StringUtils.DOT, FileType.TXT.getType() );
					
					FileUtils.copyFile( executableDir.toFile(), templateDir.toFile(), fileName,
							Strman.append( FileUtils.getFileNameWithoutExt( fileName ), StringUtils.UNDERLINE,
									modelArguments.getProjectName(), StringUtils.DOT, FileType.TXT.getType() ) );
				} catch (IOException e) {
					log.error( "NCHC RTC Preprocess ExecutableAdapter: Something worng when copy output file.", e );
					this.log( LogLevel.ERROR,
							"NCHC RTC Preprocess ExecutableAdapter: Something worng when copy output file." );
				}
			} );
			
			log.info( "NCHC RTC Preprocess ExecutableAdapter: End executable adapter of NCHC RTC preprocess." );
			this.log( LogLevel.INFO,
					"NCHC RTC Preprocess ExecutableAdapter: End executable adapter of NCHC RTC preprocess." );
		} else {
			log.error( "NCHC RTC Preprocess ExecutableAdapter: The model executable or template directory not exist." );
			this.log( LogLevel.ERROR,
					"NCHC RTC Preprocess ExecutableAdapter: The model executable or template directory not exist." );
		}
	}

}
