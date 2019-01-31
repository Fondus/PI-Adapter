package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.magiclen.magiccommand.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.flow.longtime.nchc.util.RunArguments;

/**
 * The model executable for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFExecutable extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	
	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new LTFExecutable().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		RunArguments modelArguments = (RunArguments) arguments;
		Path executablePath = Paths
				.get( Strman.append( baseDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getExecutableDir() ) );
		Preconditions.checkState( Files.exists( executablePath ),
				"NCHC LTF Executable: The executable directory is not exist." );

		Path templatePath = Paths
				.get( Strman.append( baseDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getTemplateDir() ) );
		Preconditions.checkState( Files.exists( templatePath ),
				"NCHC LTF Executable: The template directory is not exist." );

		/** Copy model input file to executable directory **/
		FileUtils.copy( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, "DATA_INP_RAIN.TXT" ),
				Strman.append( executablePath.toFile().getAbsolutePath(), StringUtils.PATH, "DATA_INP_RAIN.TXT" ) );
		FileUtils.copy( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, "DATA_INP_FLOW.TXT" ),
				Strman.append( executablePath.toFile().getAbsolutePath(), StringUtils.PATH, "DATA_INP_FLOW.TXT" ) );

		/** Copy model to executable directory from template directory **/
		FileUtils.copy( templatePath.toFile().listFiles(), executablePath.toFile() );

		Path executable = Paths.get( Strman.append( executablePath.toFile().getPath(), StringUtils.PATH,
				modelArguments.getExecutable().get( 0 ) ) );
		Preconditions.checkState( Files.exists( executablePath ),
				"NCHC LTF Executable: The executable program is not exist." );

		/** Run model **/
		Command command = new Command( executable.toFile().getAbsolutePath() );
		command.run( executablePath.toFile() );
		
		/** Backup model output file by project name **/
		try {
			this.backupOuputFile( executablePath.toAbsolutePath().toString(), "OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT", modelArguments.getProjectName() );
			this.backupOuputFile( executablePath.toAbsolutePath().toString(), "SUMMARY_RESULTS_EST_FLOW_ANN_GA-SA_MTF.TXT", modelArguments.getProjectName() );
			this.backupOuputFile( executablePath.toAbsolutePath().toString(), "OUTPUT_EST_RAIN_ANN_GA-SA_MTF.TXT", modelArguments.getProjectName() );
			this.backupOuputFile( executablePath.toAbsolutePath().toString(), "OUTPUT_EST_RAIN_FLOW_ANN_GA-SA_MTF.TXT", modelArguments.getProjectName() );	
		} catch (IOException e) {
			log.error( "NCHC LTF Executable: Backup output file has something wrong." );
			this.log( LogLevel.ERROR, "NCHC LTF Executable: Backup output file has something wrong." );
		}
		
		/** Move model output to output directory **/
		FileUtils.copy( Strman.append( executablePath.toAbsolutePath().toString(), StringUtils.PATH, "OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT" ), 
						Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH, "OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT" ));
	}

	/**
	 * Backup model output file.
	 * 
	 * @param executablePath
	 * @param outputFileName
	 * @param projectName
	 * @throws IOException
	 */
	private void backupOuputFile( String executablePath, String outputFileName, String projectName ) throws IOException {
		executablePath = Strman.append( executablePath, StringUtils.PATH );
		String backupFileName = FileUtils.getNameWithoutExt( outputFileName );
		backupFileName = Strman.append( backupFileName, StringUtils.UNDERLINE, projectName, FileType.TXT.getExtension() );
		FileUtils.copy( Strman.append( executablePath, outputFileName ), Strman.append( executablePath, backupFileName ) );
	}
}
