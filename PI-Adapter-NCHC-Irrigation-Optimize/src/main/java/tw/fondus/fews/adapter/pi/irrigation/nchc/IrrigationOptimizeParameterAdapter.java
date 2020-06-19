package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.stream.StreamUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.ParameterArguments;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.Parameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.type.CaseName;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.ModelFileNames;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.ModelUtils;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.ParameterUtils;
import tw.fondus.fews.adapter.pi.irrigation.nchc.util.WebAPIClient;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * The model parameter-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use to create model parameters, copy files and executable.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeParameterAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ParameterArguments arguments = ParameterArguments.instance();
		new IrrigationOptimizeParameterAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		ParameterArguments modelArguments = this.asArguments( arguments, ParameterArguments.class );

		String regionName = modelArguments.getRegion();
		String caseName = modelArguments.getCaseName();

		logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Prepare the model environment." );

		Path templatePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getTemplatePath() ),
				"NCHC Irrigation-Optimize ParameterAdapter: The template folder not exist." );

		Path regionPath = Prevalidated.checkExists(
				templatePath.resolve( regionName ),
				"NCHC Irrigation-Optimize ParameterAdapter: The region folder not exist." );

		Path executablePath = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getExecutablePath() ),
				"NCHC Irrigation-Optimize ParameterAdapter: The executable folder is not exist." );

		Path areaControlFilePath = Prevalidated.checkExists(
				regionPath.resolve( ModelFileNames.AREA_ORDER ),
				"NCHC Irrigation-Optimize ParameterAdapter: The area control file not exist." );

		try {
			// Connect to external system
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Load the parameters information from external system." );
			List<CaseParameter> caseParameters = WebAPIClient.get( modelArguments.getUrl(), modelArguments.getToken() );
			Optional<CaseParameter> optionalCaseParameter = ModelUtils.findCase( caseParameters, caseName );

			optionalCaseParameter.ifPresentOrElse( caseParameter -> {
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Found the case parameter: {} with external system.", caseParameter.getDescription() );

				// Check is user define or not
				Path casePath;
				if ( caseParameter.getDescription().equals( CaseName.CASE_6.getDescription() ) ){
					// User define
					int caseIndex = ModelUtils.getUserDefineCaseIndex( caseParameter );
					String userCase = Strman.append( "Case", String.valueOf( caseIndex ) );
					casePath = Prevalidated.checkExists(
							regionPath.resolve( userCase ),
							"NCHC Irrigation-Optimize ParameterAdapter: The case folder not exist." );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: The found user define case is: {}.", userCase );
				} else {
					casePath = Prevalidated.checkExists(
							regionPath.resolve( caseName ),
							"NCHC Irrigation-Optimize ParameterAdapter: The case folder not exist." );
				}

				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Start the create the parameter with region: {}, case: {}.",
						regionName, caseName );

				// Read the parameter duration
				Path parameterFilePath = Prevalidated.checkExists(
						casePath.resolve( ModelFileNames.PARAMETER ),
						"NCHC Irrigation-Optimize ParameterAdapter: The model parameter file not exist." );

				try {
					int duration = ModelUtils.readModelDuration( parameterFilePath );
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: The model case: {} mapping duration is {}.", caseName, String.valueOf( duration ) );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Create the irrigation area parameters file." );
					String areaParametersContent = this.createIrrigationAreaParameters( caseParameter, areaControlFilePath );
					ModelUtils.writeFile( inputPath, executablePath, ModelFileNames.AREA, areaParametersContent );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Create the hydraulic structures file." );
					String hydraulicStructuresContent = this.createHydraulicStructures( caseParameter, duration, modelArguments.getHydraulicStructures() );
					ModelUtils.writeFile( inputPath, executablePath, ModelFileNames.STRUCTURE, hydraulicStructuresContent );

					// Copy the template files
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Copy the template files from region: {}, case: {} folder.", regionName, caseName );
					PathUtils.copy( templatePath.resolve( ModelFileNames.MODEL ), executablePath );
					Files.list( casePath ).forEach( caseFile -> {
						String fileName = PathUtils.getName( caseFile );
						PathUtils.copy( casePath.resolve( fileName ), executablePath );
					} );

					// Executable the model
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Start running model with region: {}, case: {}.", regionName, caseName );
					String command = executablePath.resolve( ModelFileNames.MODEL ).toString();
					Executions.execute( executor ->
									executor.directory( executablePath.toFile() )
											.redirectOutput( Slf4jStream.of( "NCHC Irrigation-Optimize Model" ).asDebug() )
							,command );
					PathUtils.copy( executablePath.resolve( ModelFileNames.OUTPUT_MAIN ), outputPath );
					PathUtils.copy( executablePath.resolve( ModelFileNames.OUTPUT_SUB ), outputPath );
					PathUtils.copy( executablePath.resolve( ModelFileNames.OUTPUT_THREE ), outputPath );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Finished the ParameterAdapter." );

				} catch ( IOException e ){
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Adapter read files has IO problem." );
				} catch (InterruptedException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Running model has interrupted problem." );
				} catch (TimeoutException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Running model has timeout problem." );
				}

			}, () -> logger.log( LogLevel.WARN, "NCHC Irrigation-Optimize ParameterAdapter: Not found case parameter with user assign case." ));

		} catch ( IOException e ){
			logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Adapter connect to external system has IO problem." );
		}
	}

	/**
	 * Create the irrigation area parameters file content.
	 *
	 * @param caseParameter case parameter
	 * @param areaControlFilePath area control path
	 * @return area parameters file content
	 */
	private String createIrrigationAreaParameters( CaseParameter caseParameter, Path areaControlFilePath ) {
		List<Parameter> parameters = ParameterUtils.filterArea( caseParameter );
		Map<String, Parameter> parameterMap = ParameterUtils.toMap( parameters );

		// Read the area order file
		List<String> areaOrders = PathReader.readAllLines( areaControlFilePath );
		return areaOrders.stream()
				.filter( parameterMap::containsKey )
				.map( parameterMap::get )
				.map( parameter -> parameter.getValue().toString() )
				.collect( Collectors.joining( Strings.SPLIT_TAB ) );
	}

	/**
	 * Create the irrigation hydraulic structures file content.
	 *
	 * @param caseParameter case parameter
	 * @param duration duration
	 * @param targets target
	 * @return hydraulic structures file conten
	 */
	private String createHydraulicStructures(
			CaseParameter caseParameter, int duration,
			List<String> targets ){
		List<Parameter> parameters = ParameterUtils.filterDraft( caseParameter );
		return targets.stream()
			.flatMap( target -> StreamUtils.boxed( ModelUtils.findParameter( parameters, target ) ) )
			.map( parameter -> ModelUtils.createDurationContent( parameter.getValue(), duration ) )
			.collect( Collectors.joining( Strings.BREAKLINE ) );
	}
}
