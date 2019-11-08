package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.stream.StreamUtils;
import tw.fondus.commons.util.string.StringUtils;
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
import java.util.stream.IntStream;

/**
 * The model parameter-adapter for running NCHC irrigation-optimize model from Delft-FEWS.
 * It's use to create model parameters, copy files and executable.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeParameterAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ParameterArguments arguments = new ParameterArguments();
		new IrrigationOptimizeParameterAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		ParameterArguments modelArguments = (ParameterArguments) arguments;

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
			/** Connect to external system **/
			logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Load the parameters information from external system." );
			List<CaseParameter> caseParameters = WebAPIClient.get( modelArguments.getUrl(), modelArguments.getToken() );
			Optional<CaseParameter> optionalCaseParameter = ModelUtils.findCase( caseParameters, caseName );
			OptionalUtils.ifPresentOrElse( optionalCaseParameter, caseParameter -> {
				logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Found the case parameter: {} with external system.", caseParameter.getDescription() );

				/** Check is user define or not **/
				Path casePath;
				if ( caseParameter.getDescription().equals( CaseName.CASE_6.getDescription() ) ){
					/** User define **/
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

				/** Read the parameter duration **/
				Path parameterFilePath = Prevalidated.checkExists(
						casePath.resolve( ModelFileNames.PARAMETER ),
						"NCHC Irrigation-Optimize ParameterAdapter: The model parameter file not exist." );

				try {
					int duration = ModelUtils.readModelDuration( parameterFilePath.toString() );
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: The model case: {} mapping duration is {}.", caseName, String.valueOf( duration ) );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Create the irrigation area parameters file." );
					String areaParametersContent = this.createIrrigationAreaParameters( caseParameter, areaControlFilePath );
					ModelUtils.writeFile( inputPath, executablePath, ModelFileNames.AREA, areaParametersContent );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Create the water requirement file." );
					this.createWaterRequirements(
							logger, inputPath, executablePath,
							caseParameter, duration,
							modelArguments.getWaterRequirementTargets(), modelArguments.getWaterRequirementsFiles() );

					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Create the hydraulic structures file." );
					String hydraulicStructuresContent = this.createHydraulicStructures( caseParameter, duration, modelArguments.getHydraulicStructures() );
					ModelUtils.writeFile( inputPath, executablePath, ModelFileNames.STRUCTURE, hydraulicStructuresContent );

					/** Copy the template files **/
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Copy the template files from region: {}, case: {} folder.", regionName, caseName );
					ModelUtils.copyFile( templatePath, executablePath, ModelFileNames.MODEL );
					Files.list( casePath ).forEach( caseFile -> {
						String fileName = PathUtils.getName( caseFile );
						try {
							ModelUtils.copyFile( casePath, executablePath, fileName );
						} catch (IOException e) {
							logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Copy the template file: {} has something wrong.", fileName );
						}
					} );

					/** Executable the model **/
					logger.log( LogLevel.INFO, "NCHC Irrigation-Optimize ParameterAdapter: Start running model with region: {}, case: {}.", regionName, caseName );
					String command = executablePath.resolve( ModelFileNames.MODEL ).toString();
					Executions.execute( executor ->
									executor.directory( executablePath.toFile() )
											.redirectOutput( Slf4jStream.of( "NCHC Irrigation-Optimize Model" ).asDebug() )
							,command );
					PathUtils.copy( executablePath.resolve( ModelFileNames.OUTPUT ), outputPath );

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
	 * @param caseParameter
	 * @param areaControlFilePath
	 * @return
	 */
	private String createIrrigationAreaParameters( CaseParameter caseParameter, Path areaControlFilePath  ) throws IOException {
		List<Parameter> parameters = ParameterUtils.filterArea( caseParameter );
		Map<String, Parameter> parameterMap = ParameterUtils.toMap( parameters );

		/** Read the area order file **/
		List<String> areaOrders = ModelUtils.readAreaOrder( areaControlFilePath.toString() );
		return areaOrders.stream()
				.filter( order -> parameterMap.containsKey( order ) )
				.map( order -> parameterMap.get( order ) )
				.map( parameter -> parameter.getValue().toString() )
				.collect( Collectors.joining( StringUtils.TAB ) );
	}

	/**
	 * Create the model water requirement files.
	 *
	 * @param logger
	 * @param inputPath
	 * @param executablePath
	 * @param caseParameter
	 * @param duration
	 * @param targets
	 * @param files
	 */
	private void createWaterRequirements(
			PiDiagnosticsLogger logger,
			Path inputPath, Path executablePath,
			CaseParameter caseParameter, int duration,
			List<String> targets, List<String> files ){
		List<Parameter> parameters = ParameterUtils.filterPlanWaterRequirement( caseParameter );
		IntStream.range( 0, targets.size() ).forEach( i -> {
			Optional<Parameter> opt = ModelUtils.findParameter( parameters, targets.get( i ) );
			opt.ifPresent( parameter -> {
				String fileName = files.get( i );
				String content = ModelUtils.createDurationContent( parameter.getValue(), duration );

				try {
					ModelUtils.writeFile( inputPath, executablePath, fileName, content );
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "NCHC Irrigation-Optimize ParameterAdapter: Create the water requirement file: {} has IO problem.", fileName );
				}
			} );
		} );
	}

	/**
	 * Create the irrigation hydraulic structures file content.
	 *
	 * @param caseParameter
	 * @param duration
	 * @param targets
	 * @return
	 */
	private String createHydraulicStructures(
			CaseParameter caseParameter, int duration,
			List<String> targets ){
		List<Parameter> parameters = ParameterUtils.filterDraft( caseParameter );
		return targets.stream()
			.flatMap( target -> StreamUtils.streamBoxed( ModelUtils.findParameter( parameters, target ) ) )
			.map( parameter -> ModelUtils.createDurationContent( parameter.getValue(), duration ) )
			.collect( Collectors.joining( StringUtils.BREAKLINE ) );
	}
}