package tw.fondus.fews.adapter.pi.wflow;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.locationtech.jts.geom.Coordinate;
import org.zeroturnaround.exec.InvalidExitValueException;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.util.gson.GsonMapper;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.minio.MinioHighLevelClient;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesCollection;
import tw.fondus.commons.rest.pi.json.util.timeseries.PiSeriesMapper;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.jts.JTSUtils;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.file.zip.ZipUtils;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.vo.index.Index2D;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.aws.storage.util.PredefinePrefixUtils;
import tw.fondus.fews.adapter.pi.aws.storage.util.S3ProcessUtils;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;
import tw.fondus.fews.adapter.pi.wflow.argument.MulticaseArguments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The main process of WFlow model multi-case run.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class MulticaseRunUploadS3Process extends PiCommandLineExecute {
	private GsonMapper mapper;

	public static void main( String[] args ) {
		MulticaseArguments arguments = MulticaseArguments.instance();
		new MulticaseRunUploadS3Process().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		MulticaseArguments processArguments = this.asArguments( arguments, MulticaseArguments.class );

		Path parameterPath = Prevalidated.checkExists( basePath.resolve( processArguments.getParameterPath() ),
				"MulticaseRunUploadS3Process: Can not find the parameter directory." );

		Path modelPath = Prevalidated.checkExists( basePath.resolve( "model" ),
				"MulticaseRunUploadS3Process: Can not find the model directory of WFlow." );

		Path modelParameterPath = Prevalidated.checkExists( modelPath.resolve( "intbl" ),
				"MulticaseRunUploadS3Process: Can not find the parameter directory of WFlow." );

		Path modelOutputPath = Prevalidated.checkExists( modelPath.resolve( "SBM" ).resolve( "outmaps" ),
				"MulticaseRunUploadS3Process: Can not find the output directory of WFlow." );

		Path modelInputPath = Prevalidated.checkExists( modelPath.resolve( "inmaps" ),
				"MulticaseRunUploadS3Process: Can not find the input directory of WFlow." );

		Path waterLevelAttributePath = Prevalidated.checkExists(
				inputPath.resolve( processArguments.getInputs().get( 0 ) ),
				"MulticaseRunUploadS3Process: Can not find the water level attribute CSV file." );
		Map<String, Coordinate> coordinateMap = PathReader.readAllLines( waterLevelAttributePath )
				.stream()
				.map( line -> line.split( Strings.COMMA ) )
				.collect( Collectors.toMap( split -> split[0], split -> JTSUtils
						.coordinate( NumberUtils.create( split[2] ), NumberUtils.create( split[3] ) ) ) );
		Path rainfallPath = Prevalidated.checkExists( modelInputPath.resolve( processArguments.getInputs().get( 1 ) ),
				"MulticaseRunUploadS3Process: Can not find rainfall NetCDF file of model input." );
		PathUtils.copy( rainfallPath, inputPath.resolve( processArguments.getInputs().get( 2 ) ) );
		Path catchmentRainfallPath = Prevalidated.checkExists(
				modelInputPath.resolve( processArguments.getInputs().get( 3 ) ),
				"MulticaseRunUploadS3Process: Can not find catchment rainfall XML file of model input." );
		PathUtils.copy( catchmentRainfallPath, inputPath );

		Path waterLevelPath = Prevalidated.checkExists( modelInputPath.resolve( processArguments.getInputs().get( 4 ) ),
				"MulticaseRunUploadS3Process: Can not find obervation water level file." );

		this.mapper = GsonMapperRuntime.ISO8601;

		try {
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( waterLevelPath );
			PiTimeSeriesCollection piTimeSeriesCollection = PiSeriesMapper.toPiTimeSeriesCollection( timeSeriesArrays );
			PathWriter.write( inputPath.resolve( "Observation.json" ), this.mapper.toString( piTimeSeriesCollection ) );

			String host = processArguments.getHost();
			String bucket = processArguments.getBucket();
			String username = processArguments.getUsername();
			String password = processArguments.getPassword();
			MinioHighLevelClient client = MinioHighLevelClient.builder()
					.client( MinioClient.builder().endpoint( host ).credentials( username, password ).build() )
					.defaultBucket( bucket )
					.build();

			List<Path> parameterPaths = PathUtils.list( parameterPath );
			String prefixTemplate = processArguments.getObjectPrefix();
			parameterPaths.stream().filter( path -> PathUtils.equalsExtension( path, FileType.ZIP ) ).forEach( path -> {
				logger.log( LogLevel.INFO, "MulticaseRunUploadS3Process: Running case {}",
						PathUtils.getNameWithoutExtension( path ) );
				ZipUtils.unzip( path, modelParameterPath );

				logger.log( LogLevel.INFO, "MulticaseRunUploadS3Process: Running WFlow model." );
				try {
					PathUtils.clean( modelOutputPath );
					PathUtils.clean( outputPath );
					Executions.execute( executor -> executor.directory( basePath.toFile() ), "docker-compose", "run",
							"--rm", "WFlow" );

					Path modelOutputNC = Prevalidated.checkExists(
							modelOutputPath.resolve( processArguments.getOutputs().get( 0 ) ),
							"MulticaseRunUploadS3Process: Can not find the output file of WFlow." );
					PathUtils.copy( modelOutputNC, outputPath.resolve( processArguments.getOutputs().get( 1 ) ) );
					this.getFlowDataFromModelOutput( modelOutputNC, coordinateMap, timeSeriesArrays,
							outputPath.resolve( processArguments.getOutputs().get( 2 ) ) );
				} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
					logger.log( LogLevel.ERROR,
							"MulticaseRunUploadS3Process: Running WFlow model has something wrong." );
				}

				try {
					S3ProcessUtils.isCreateS3BucketBefore( "MulticaseRunUploadS3Process", logger, client, bucket,
							processArguments.isCreate() );

					logger.log( LogLevel.INFO,
							"MulticaseRunUploadS3Process: Start to upload folder: {} and {} with S3 API.",
							modelInputPath, outputPath );
					if ( client.isExistsBucket() ) {
						processArguments.setObjectPrefix( Strman.append( prefixTemplate, Strings.UNDERLINE,
								PathUtils.getNameWithoutExtension( path ), Strings.SLASH ) );
						PathUtils.list( inputPath ).forEach( p -> {
							String prefix = PredefinePrefixUtils.createPredefinePrefix( processArguments, path );
							String object = prefix + PathUtils.getName( p );
							S3ProcessUtils.uploadS3Object( "MulticaseRunUploadS3Process", logger, client, object, p );
						} );

						PathUtils.list( outputPath ).forEach( p -> {
							String prefix = PredefinePrefixUtils.createPredefinePrefix( processArguments, p );
							String object = prefix + PathUtils.getName( p );
							S3ProcessUtils.uploadS3Object( "MulticaseRunUploadS3Process", logger, client, object, p );
						} );

						String prefix = PredefinePrefixUtils.createPredefinePrefix( processArguments, path );
						String object = prefix + processArguments.getOutputs().get( 3 );
						S3ProcessUtils.uploadS3Object( "MulticaseRunUploadS3Process", logger, client, object, path );
					} else {
						logger.log( LogLevel.WARN,
								"MulticaseRunUploadS3Process: The target bucket: {} not exist, will ignore the adapter process.",
								bucket );
					}
					logger.log( LogLevel.INFO,
							"MulticaseRunUploadS3Process: Finished to upload folder: {} with S3 API.", inputPath );
				} catch (MinioException e) {
					logger.log( LogLevel.ERROR,
							"MulticaseRunUploadS3Process: Working with S3 API has something wrong! {}", e );
				}

			} );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR,
					"MulticaseRunUploadS3Process: Reading time series XML file has something wrong." );
		}
	}

	/**
	 * Get flow data from NetCDF of model output.
	 * 
	 * @param logger diagnostics logger
	 * @param modelOutputNC model output NetCDF
	 * @param coordinateMap flow location coordinate map
	 * @param timeSeriesArrays observation flow time series arrays
	 * @param outputJSONPath output simulation JSON path
	 */
	private void getFlowDataFromModelOutput( Path modelOutputNC, Map<String, Coordinate> coordinateMap,
			TimeSeriesArrays timeSeriesArrays, Path outputJSONPath ) {
		try (NetCDFReader reader = NetCDFReader.read( modelOutputNC ) ) {
			List<StandardGrid> grids = NetCDFGridMapper.fromTYXModel( reader, "runR" );

			this.getLogger().log( LogLevel.INFO, "MulticaseRunUploadS3Process: Getting flow value from model output." );
			PiTimeSeriesCollection outputCollection = PiSeriesMapper.toPiTimeSeriesCollection( timeSeriesArrays );
			outputCollection.getCollection().forEach( collection -> {
				String id = collection.getHeader().getLocationId();
				Optional<Coordinate> optCoordinate = Optional.ofNullable( coordinateMap.get( id ) );
				optCoordinate.ifPresentOrElse( coordinate -> {
					IntStream.range( 0, grids.size() ).forEach( i -> {
						// fill nearest index value or missing
						StandardGrid grid = grids.get( i );
						Optional<Index2D> optIndex = grid.findNearestIndex( coordinate, false );
						optIndex.ifPresentOrElse( index -> {
							collection.get( i )
									.setValue( grid.value( grid.findNearestIndex( coordinate, false ).get().getCol(),
											grid.findNearestIndex( coordinate, false ).get().getRow() ) );
						}, () -> {
							collection.get( i ).setValue( Numbers.MISSING );
						} );
					} );
				}, () -> {
					// fill missing value if not found coordinate from map
					IntStream.range( 0, grids.size() ).forEach( i -> {
						collection.get( i ).setValue( Numbers.MISSING );
					} );
				} );
			} );

			PathWriter.write( outputJSONPath, this.mapper.toString( outputCollection ) );
		} catch (IOException e) {
			this.getLogger().log( LogLevel.INFO, "MulticaseRunUploadS3Process: Reading NetCDF has something wrong." );
		}
	}
}
