package tw.fondus.fews.adapter.pi.rtc.nchc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;

/**
 * The file utils for project PI-Adapter-NCHC-RTC-2D.
 * 
 * @author Chao
 *
 */
public class FileTools {
	/**
	 * Copy file with same file name and different directory.
	 * 
	 * @param inputPath
	 * @param outputPath
	 * @param fileName
	 * @throws IOException
	 */
	public static void copyFile( Path inputPath, Path outputPath, String fileName ) throws IOException {
		copyFile( inputPath, outputPath, fileName, fileName );
	}

	/**
	 * Copy file with different file name and directory.
	 * 
	 * @param inputPath
	 * @param outputPath
	 * @param inputFileName
	 * @param outputFileName
	 * @throws IOException
	 */
	public static void copyFile( Path inputPath, Path outputPath, String inputFileName, String outputFileName ) throws IOException {
		Files.copy( Paths.get( Strman.append( inputPath.toString(), StringUtils.PATH, inputFileName ) ),
				Paths.get( Strman.append( outputPath.toString(), StringUtils.PATH, outputFileName ) ), StandardCopyOption.REPLACE_EXISTING );
	}

	/**
	 * Backup model output file to directory of template from executable.
	 * 
	 * @param executableDir
	 * @param templateDir
	 * @param backupFileName
	 * @param projectName
	 * @param timeStep
	 * @throws IOException
	 */
	public static void backupOutputFile( Path executableDir, Path templateDir, String backupFileName,
			String projectName, int timeStep ) throws IOException {
		backupOutputFile( executableDir, templateDir, backupFileName, projectName, timeStep, FileType.TXT );
	}

	/**
	 * Backup model output file to directory of template from executable with different file extension.
	 * 
	 * @param executableDir
	 * @param templateDir
	 * @param backupFileName
	 * @param projectName
	 * @param timeStep
	 * @param fileType
	 * @throws IOException
	 */
	public static void backupOutputFile( Path executableDir, Path templateDir, String backupFileName,
			String projectName, int timeStep, FileType fileType ) throws IOException {
		copyFile( executableDir, templateDir, backupFileName,
				Strman.append( FileUtils.getNameWithoutExt( backupFileName ), StringUtils.UNDERLINE, "T1",
						String.format( "%03d", timeStep ), StringUtils.UNDERLINE, projectName, StringUtils.DOT,
						fileType.getType() ) );
	}

	/**
	 * Get ASC file name from mapStacks file by time step.
	 * 
	 * @param fileName
	 * @param timeStep
	 * @return
	 */
	public static String getSimulationASCFileName( String fileName, int timeStep ) {
		return Strman.append( fileName.substring( 0, fileName.indexOf( StringUtils.QUESTION ) ), "0000", StringUtils.DOT,
				String.format( "%03d", timeStep ) );
	}
}
