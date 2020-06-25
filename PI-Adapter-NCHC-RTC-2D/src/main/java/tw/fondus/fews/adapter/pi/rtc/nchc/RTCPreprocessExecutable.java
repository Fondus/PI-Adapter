package tw.fondus.fews.adapter.pi.rtc.nchc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.zeroturnaround.exec.InvalidExitValueException;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.rtc.nchc.argument.RunArguments;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.CommonString;
import tw.fondus.fews.adapter.pi.rtc.nchc.util.FileTools;

/**
 * Model preprocess executable-adapter for running NCHC RTC model from
 * Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreprocessExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		RunArguments arguments = RunArguments.instance();
		new RTCPreprocessExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path executableDir = Prevalidated.checkExists( basePath.resolve( modelArguments.getExecutableDir() ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find executable directory." );
		
		Path templateDir = Prevalidated.checkExists( basePath.resolve( modelArguments.getTemplateDir() ),
				"NCHC RTC Preprocess ExecutableAdapter: Can not find template directory." );
		
		logger.log( LogLevel.INFO, "NCHC RTC Preprocess ExecutableAdapter: Start executable adapter of NCHC RTC preprocess." );
		logger.log( LogLevel.INFO, "NCHC RTC Preprocess ExecutableAdapter: Copy input and template file to executable directory." );
		try (Stream<Path> paths = Files.list( templateDir ) ) {
			FileTools.copyFile( inputPath, executableDir, CommonString.INPUT_CORR_SIM_WH );
			FileTools.copyFile( inputPath, executableDir, CommonString.INPUT_WH_EST_OBS_GAUGES );

			paths.forEach( path -> {
				try {
					FileTools.copyFile( templateDir, executableDir, path.getFileName().toString() );
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "NCHC RTC Preprocess ExecutableAdapter: Copying template file has something worng." );
				}
			} );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC RTC Preprocess ExecutableAdapter: Copying input file has something worng." );
		}
		
		/** Run model pre process **/
		logger.log( LogLevel.INFO, "NCHC RTC Preprocess ExecutableAdapter: Start preprocess." );
		String command = executableDir.resolve( modelArguments.getExecutable().get( 0 ) ).toAbsolutePath().toString();
		try {
			Executions.execute( executor -> executor.directory( executableDir.toFile() ),
					command );
		} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e1) {
			logger.log( LogLevel.ERROR, "NCHC RTC Preprocess ExecutableAdapter: The preprocess has something wrong." );
		}
		
		/** Backup data of model preprocess output **/
		IntStream.rangeClosed( 0, modelArguments.getForecast() ).forEach( timeStep -> {
			try {
				String fileName = Strman.append( CommonString.INPUT_VAL_GRIDS_OBS_EST_T,
						String.format( "%03d", timeStep ), FileType.TXT.getExtension() );

				FileTools.copyFile( executableDir, executableDir, fileName,
						Strman.append( FileUtils.getNameWithoutExt( fileName ), Strings.UNDERLINE,
								modelArguments.getProjectName(), FileType.TXT.getExtension() ) );
				
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "NCHC RTC Preprocess ExecutableAdapter: Something worng when copy output file." );
			}
		} );

		logger.log( LogLevel.INFO, "NCHC RTC Preprocess ExecutableAdapter: End executable adapter of NCHC RTC preprocess." );
	}
}
