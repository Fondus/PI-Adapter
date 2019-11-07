package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import com.google.common.base.Preconditions;
import nl.wldelft.util.FileUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.Parameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.type.CaseName;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The tools of model files.
 *
 * @author Brad Chen
 *
 */
public class ModelUtils {
	private ModelUtils(){}

	/**
	 * Get the target case parameter.
	 *
	 * @param caseParameters
	 * @param targetCase
	 * @return
	 */
	public static Optional<CaseParameter> findCase( List<CaseParameter> caseParameters, String targetCase ){
		return findCaseName( targetCase )
				.map(
						caseName ->
								caseParameters
									.stream()
									.filter( caseParameter -> caseParameter.getDescription().equals( caseName.getDescription() ) )
									.findFirst()
				).orElse( null );
	}

	/**
	 * Find the parameter by target key word.
	 *
	 * @param parameters
	 * @param target
	 * @return
	 */
	public static Optional<Parameter> findParameter( List<Parameter> parameters, String target ){
		return parameters.stream()
				.filter( parameter -> parameter.getDescription().contains( target ) )
				.findFirst();
	}

	/**
	 * Get the user define case index.
	 *
	 * @param caseParameter
	 * @return
	 */
	public static int getUserDefineCaseIndex( CaseParameter caseParameter ){
		Parameter parameter = ParameterUtils.filterUserDefine( caseParameter ).get( 0 );
		int value = parameter.getValue().intValue();
		return ( value > 0 && value <=5 ) ? value : 2;
	}

	/**
	 * Read area order from the control file.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<String> readAreaOrder( String file ) throws IOException {
		Path path = get( file );
		return PathUtils.readAllLines( path, StringUtils.UTF8_CHARSET );
	}

	/**
	 * Read the time duration from the model parameter file.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static int readModelDuration( String file ) throws IOException {
		Path path = get( file );
		List<String> lines = PathUtils.readAllLines( path, StringUtils.UTF8_CHARSET );
		Preconditions.checkArgument( lines.size() > 0 && lines.size() > 13, "ModelUtils: The model parameter file content not correct." );
		return Integer.parseInt( lines.get( 12 ).split( StringUtils.SPACE_MULTIPLE )[0] );
	}

	/**
	 * Write the file with content, and copy to output path.
	 *
	 * @param inputPath
	 * @param outputPath
	 * @param fileName
	 * @param fileContent
	 * @throws IOException
	 */
	public static void writeFile( Path inputPath, Path outputPath, String fileName, String fileContent ) throws IOException {
		Path writePath = inputPath.resolve( fileName );
		FileUtils.writeText( writePath.toString(), fileContent );
		PathUtils.copy( writePath, outputPath );
	}

	/**
	 * Copy the file to the target path.
	 *
	 * @param inputPath
	 * @param outputPath
	 * @param fileName
	 * @throws IOException
	 */
	public static void copyFile( Path inputPath, Path outputPath, String fileName ) throws IOException{
		PathUtils.copy( inputPath.resolve( fileName ), outputPath );
	}

	/**
	 * Create the duration content.
	 *
	 * @param value
	 * @param duration
	 * @return
	 */
	public static String createDurationContent( BigDecimal value, int duration ){
		return IntStream.range( 0, duration )
				.mapToObj( i -> value.toString() )
				.collect( Collectors.joining( StringUtils.TAB ) );
	}

	/**
	 * Find the case name by target case.
	 *
	 * @param targetCase
	 * @return
	 */
	private static Optional<CaseName> findCaseName( String targetCase ){
		return Stream.of( CaseName.values() ).filter( caseName -> caseName.getName().equals( targetCase ) ).findFirst();
	}

	/**
	 * Get path and check file exist.
	 *
	 * @param file
	 * @return
	 */
	private static Path get( String file ){
		Path path = Paths.get( file );
		Preconditions.checkState( PathUtils.exists( path ), "ModelUtils: The file not exist!" );
		return path;
	}
}
