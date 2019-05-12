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
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.FileUtils;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.RunArguments;

/**
 * Model preprocess executable-adapter for running NCHC RTC model from
 * Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreprocessExecutable extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger( this.getClass() );

	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new RTCPreprocessExecutable().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		RunArguments modelArguments = (RunArguments) arguments;

		Path executableDir = Paths
				.get( Strman.append( baseDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getExecutableDir() ) );
		Preconditions.checkState( Files.exists( executableDir ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find executable directory." );

		Path templateDir = Paths
				.get( Strman.append( baseDir.getPath(), StringUtils.PATH, modelArguments.getTemplateDir() ) );
		Preconditions.checkState( Files.exists( templateDir ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find template directory." );

		log.info( "NCHC RTC Preprocess ExecutableAdapter: Start executable adapter of NCHC RTC preprocess." );
		this.log( LogLevel.INFO,
				"NCHC RTC Preprocess ExecutableAdapter: Start executable adapter of NCHC RTC preprocess." );

		/**
		 * Copy model input file to executable directory from input directory
		 **/
		log.info( "NCHC RTC Preprocess ExecutableAdapter: Copy input and template file to executable directory." );
		this.log( LogLevel.INFO,
				"NCHC RTC Preprocess ExecutableAdapter: Copy input and template file to executable directory." );
		try (Stream<Path> paths = Files.list( templateDir ) ) {
			FileUtils.copyFile( inputDir, executableDir.toFile(), CommonString.INPUT_CORR_SIM_WH );
			FileUtils.copyFile( inputDir, executableDir.toFile(), CommonString.INPUT_WH_EST_OBS_GAUGES );

			paths.forEach( path -> {
				try {
					FileUtils.copyFile( templateDir.toFile(), executableDir.toFile(), path.getFileName().toString() );
				} catch (IOException e) {
					log.error( "NCHC RTC Preprocess ExecutableAdapter: Copying template file has something worng.", e );
					this.log( LogLevel.ERROR,
							"NCHC RTC Preprocess ExecutableAdapter: Copying template file has something worng." );
				}
			} );
		} catch (IOException e) {
			log.error( "NCHC RTC Preprocess ExecutableAdapter: Copying input file has something worng.", e );
			this.log( LogLevel.ERROR,
					"NCHC RTC Preprocess ExecutableAdapter: Copying input file has something worng." );
		}

		/** Run model preprocess **/
		log.info( "NCHC RTC Preprocess ExecutableAdapter: Start preprocess." );
		this.log( LogLevel.INFO, "NCHC RTC Preprocess ExecutableAdapter: Start preprocess." );
		Command command = new Command( Strman.append( executableDir.toFile().getPath(), StringUtils.PATH,
				modelArguments.getExecutable().get( 0 ) ) );
	
		command.run( executableDir.toFile() );
		System.out.println( command.toString() );
		/** Backup data of model preprocess output **/
		IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> {
			try {
				String fileName = Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T,
						String.format( "%03d", timeStep ), FileType.TXT.getExtension() );

				FileUtils.copyFile( executableDir.toFile(), executableDir.toFile(), fileName,
						Strman.append( FileUtils.getFileNameWithoutExt( fileName ), StringUtils.UNDERLINE,
								modelArguments.getProjectName(), FileType.TXT.getExtension() ) );
			} catch (IOException e) {
				log.error( "NCHC RTC Preprocess ExecutableAdapter: Something worng when copy output file.", e );
				this.log( LogLevel.ERROR,
						"NCHC RTC Preprocess ExecutableAdapter: Something worng when copy output file." );
			}
		} );

		log.info( "NCHC RTC Preprocess ExecutableAdapter: End executable adapter of NCHC RTC preprocess." );
		this.log( LogLevel.INFO,
				"NCHC RTC Preprocess ExecutableAdapter: End executable adapter of NCHC RTC preprocess." );
	}

}
