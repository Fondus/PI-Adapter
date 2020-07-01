package tw.fondus.fews.adapter.pi.search.wrap.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import strman.Strman;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;

/**
 * The tools is base GADL library to develop.
 * 
 * @author shepherd
 * @author Brad Chen :improve the code
 */
public class GDALUtils {
	private static final String AAIGRID_STRING = "AAIGrid";
	private static final String GTIFF_STRING = "GTiff";

	/**
	 * Use in grid files merge.
	 * 
	 * @param GDALPath
	 * @param mergedDataPath
	 * @param exportPath
	 * @param fileName
	 * @param inputFormat
	 * @throws IOException
	 */
	public static void merge( String GDALPath, String mergedDataPath, String exportPath, String fileName,
			String inputFormat ) throws IOException {

		List<String> mergeData = readDataListAttachedFormat( mergedDataPath, checkFileType( inputFormat ) );
		StringJoiner joiner = new StringJoiner( Strings.SPACE );
		joiner.add( Strman.append( GDALPath, Strings.PATH, "pythonw.exe" ) );
		joiner.add( Strman.append( GDALPath, Strings.PATH, "gdal_merge.py" ) );
		joiner.add( "-of" );
		joiner.add( "GTiff" );
		joiner.add( "-o" );
		joiner.add( Strman.append( exportPath, Strings.PATH, fileName, ".tif" ) );
		joiner.add( "-a_nodata" );
		joiner.add( "-999" );
		mergeData.forEach( data -> {
			joiner.add( data );
		} );
		joiner.add( Strings.BREAKLINE );
		joiner.add( "exit" );
		Path exportFilePath = Paths.get( Strman.append( mergedDataPath, Strings.PATH, "mergeBatRun.bat" ) );
		writeFile( exportFilePath, joiner.toString() );
		runBatch( exportFilePath );
	}

	/**
	 * Use in grid file type transformation.
	 * 
	 * @param GDALPath
	 * @param transformationDataPath
	 * @param exportPath
	 * @param inputFormat
	 * @param outputFormat
	 * @throws IOException
	 */
	public static void transformation( String GDALPath, String transformationDataPath, String exportPath,
			String inputFormat, String outputFormat ) throws IOException {
		List<String> transformationData = readDataListAttachedFormat( transformationDataPath,
				checkFileType( inputFormat ) );
		StringJoiner joiner = new StringJoiner( Strings.SPACE );
		transformationData.forEach( data -> {
			joiner.add( Strman.append( GDALPath, Strings.PATH, "gdal_translate.exe" ) );
			joiner.add( "-a_nodata" );
			joiner.add( "-999" );
			joiner.add( "-of" );
			joiner.add( outputFormat );
			joiner.add( data );
			String dataName = Paths.get( data ).toFile().getName();
			joiner.add( Strman.append( exportPath, Strings.PATH,
					dataName.substring( 0, dataName.indexOf( Strings.DOT ) ), Strings.DOT,
					checkFileType( outputFormat ) ) );
			joiner.add( Strings.BREAKLINE );
		} );

		joiner.add( "exit" );
		Path transformationFilePath = Paths
				.get( Strman.append( transformationDataPath, Strings.PATH, "transformationBatRun.bat" ) );
		writeFile( transformationFilePath, joiner.toString() );
		runBatch( transformationFilePath );
	}

	private static void runBatch( Path path ) throws IOException {
		try {
			Process rnuBat = Runtime.getRuntime().exec( Strman.append( "cmd /c start /wait ", path.toString() ) );
			rnuBat.waitFor();
		} catch (InterruptedException e) {
			throw new IOException( "Runbat has something wrong." );

		}
	}

	/**
	 * Follow extension to get file list.
	 * 
	 * @param path
	 * @param format
	 * @return
	 * @throws IOException 
	 */
	private static List<String> readDataListAttachedFormat( String path, String format ) throws IOException {
		try ( Stream<Path> paths = Files.list( Paths.get( path ) ) ){
			return paths.filter( filePath -> PathUtils.getFileExtension( filePath ).equals( format ) )
					.map( filePath -> filePath.toString() )
					.collect( Collectors.toList() );
		}
	}

	/**
	 * Check file extension.
	 * 
	 * @param fileName
	 * @return
	 */
	private static String checkFileType( String fileName ) {
		String fileType = "";
		if ( fileName.equals( AAIGRID_STRING ) ) {
			fileType = FileType.ASC.getType();
		} else if ( fileName.equals( GTIFF_STRING ) ) {
			fileType = "tif";
		}
		return fileType;
	}

	/**
	 * Write content to file.
	 * 
	 * @param path
	 * @param content
	 * @throws IOException
	 */
	private static void writeFile( Path path, String content ) throws IOException {
		try ( FileWriter fw = new FileWriter( path.toFile() ) ) {
			fw.write( content );
		} catch (IOException e) {
			throw new IOException( "Write File has something wrong." );
		}
	}
}
