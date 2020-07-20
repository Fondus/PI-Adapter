package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import com.google.common.base.Preconditions;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.Parameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.type.CaseName;

import java.math.BigDecimal;
import java.nio.file.Path;
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
	 * @param caseParameters case parameters
	 * @param targetCase target case
	 * @return case parameter, it's optional
	 */
	public static Optional<CaseParameter> findCase( List<CaseParameter> caseParameters, String targetCase ){
		return findCaseName( targetCase )
				.flatMap(
						caseName ->
								caseParameters
									.stream()
									.filter( caseParameter -> caseParameter.getDescription().equals( caseName.getDescription() ) )
									.findFirst()
				);
	}

	/**
	 * Find the parameter by target key word.
	 *
	 * @param parameters parameters
	 * @param target target
	 * @return parameter, it's optional
	 */
	public static Optional<Parameter> findParameter( List<Parameter> parameters, String target ){
		return parameters.stream()
				.filter( parameter -> parameter.getDescription().contains( target ) )
				.findFirst();
	}

	/**
	 * Get the user define case index.
	 *
	 * @param caseParameter case parameter
	 * @return user define case index
	 */
	public static int getUserDefineCaseIndex( CaseParameter caseParameter ){
		Parameter parameter = ParameterUtils.filterUserDefine( caseParameter ).get( 0 );
		int value = parameter.getValue().intValue();
		return ( value > 0 && value <=5 ) ? value : 2;
	}

	/**
	 * Read the time duration from the model parameter file.
	 *
	 * @param path path
	 * @return model duration
	 */
	public static int readModelDuration( Path path ) {
		List<String> lines = PathReader.readAllLines( path, Strings.UTF8_CHARSET );
		Preconditions.checkArgument( lines.size() > 0 && lines.size() > 13, "ModelUtils: The model parameter file content not correct." );
		return Integer.parseInt( lines.get( 12 ).split( Strings.SPLIT_SPACE_MULTIPLE )[0] );
	}

	/**
	 * Write the file with content, and copy to output path.
	 *
	 * @param inputPath input path
	 * @param outputPath output path
	 * @param fileName file name
	 * @param content file content
	 */
	public static void writeFile( Path inputPath, Path outputPath, String fileName, String content ) {
		Path writePath = inputPath.resolve( fileName );
		PathWriter.write( writePath, content );
		PathUtils.copy( writePath, outputPath );
	}

	/**
	 * Create the duration content.
	 *
	 * @param value value
	 * @param duration duration
	 * @return duration content
	 */
	public static String createDurationContent( BigDecimal value, int duration ){
		return IntStream.range( 0, duration )
				.mapToObj( i -> value.toString() )
				.collect( Collectors.joining( Strings.SPLIT_TAB ) );
	}

	/**
	 * Find the case name by target case.
	 *
	 * @param targetCase target case
	 * @return case name, it's optional
	 */
	private static Optional<CaseName> findCaseName( String targetCase ){
		return Stream.of( CaseName.values() ).filter( caseName -> caseName.getName().equals( targetCase ) ).findFirst();
	}
}
