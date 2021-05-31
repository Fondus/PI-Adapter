package tw.fondus.fews.adapter.pi.grid.correct;

import com.google.common.base.Preconditions;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.fews.pi.util.transformation.Aggregations;
import tw.fondus.commons.json.geojson.model.feature.FeatureCollection;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.geojson.GeoJsonMapper;
import tw.fondus.commons.spatial.util.jts.JTSUtils;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.grid.correct.argument.RunArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for running correct grid with feature threshold wth Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class GridCorrectFeatureAccumulateThresholdAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		RunArguments arguments = RunArguments.instance();
		new GridCorrectFeatureAccumulateThresholdAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );

		Preconditions.checkState( modelArguments.getInputs().size() == 2,
				"GridCorrectFeatureThresholdAdapter: The input.xml and Grid NC name not give by command." );

		// Prepare the file
		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get(0) ),
				"GridCorrectFeatureThresholdAdapter: The input.xml do not exists!" );

		Path inputNC = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get(1) ),
				"GridCorrectFeatureThresholdAdapter: The input.nc do not exists!" );

		Path featureFolder = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getFeaturePath() ),
				"GridCorrectFeatureThresholdAdapter: The features folder do not exists!" );

		Path featureFile = Prevalidated.checkExists(
				featureFolder.resolve( modelArguments.getFeatureFile() ),
				"GridCorrectFeatureThresholdAdapter: The features file do not exists in features folder." );

		logger.log( LogLevel.INFO, "GridCorrectFeatureThresholdAdapter: Start the correct adapter process." );

		Map<String, Geometry> geometryMap = this.readFeatures( featureFile );
		if ( geometryMap.isEmpty() ){
			logger.log( LogLevel.WARN, "GridCorrectFeatureThresholdAdapter: Not contain any features with file: {}", featureFile.toString() );
		} else {
			try {
				int duration = modelArguments.getDuration();
				TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.read( inputXML );
				if ( timeSeriesArrays.get( 0 ).size() < duration ){
					logger.log( LogLevel.ERROR, "GridCorrectFeatureThresholdAdapter: Input time series period not exceed duration: {}!", duration );
				} else {
					Map<String, BigDecimal> aggregationMap = this.aggregationTimeSeries( timeSeriesArrays, duration );
					try {
						List<StandardGrid> grids = this.correctGrids( inputNC, geometryMap, aggregationMap,
								NumberUtils.create( modelArguments.getThreshold() ), modelArguments.getParameter() );
						if ( grids.isEmpty() ){
							logger.log( LogLevel.WARN, "GridCorrectFeatureThresholdAdapter: Correct the grids: {} has something wrong!", inputNC.toString() );
						} else {
							BigDecimal scale = grids.get( 0 ).getScale();
							NetCDFGridMapper.toTYXModel( outputPath.resolve( modelArguments.getOutputs().get( 0 ) ),
									"Corrected", grids, NumberUtils.notEquals( scale, Numbers.ONE ) );
						}
					} catch (IOException | InvalidRangeException e) {
						logger.log( LogLevel.ERROR, "GridCorrectFeatureThresholdAdapter: Read or Write the grids: {} has something wrong!", inputNC.toString() );
					}
				}
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "GridCorrectFeatureThresholdAdapter: No time series found in file in the model input files!" );
			}
		}

		logger.log( LogLevel.INFO, "GridCorrectFeatureThresholdAdapter:: End the correct adapter process." );
	}

	/**
	 * Read the features file as map contain id and geometry.
	 *
	 * @param featureFile file contain the features
	 * @return map contain id and geometry
	 */
	private Map<String, Geometry> readFeatures( Path featureFile ){
		this.getLogger().log( LogLevel.INFO, "GridCorrectFeatureThresholdAdapter: try to read the features from file: {}.", featureFile.toString() );
		FeatureCollection collection = GsonMapperRuntime.GEOJSON.toBean( PathReader.readString( featureFile ), FeatureCollection.class );
		return collection.stream()
			.collect( Collectors.toMap(
					feature -> feature.getPropertyAsString( Strings.ID.toUpperCase() ),
					feature -> GeoJsonMapper.toJTS( feature.getGeometry() )
			));
	}

	/**
	 * Aggregation the timeseries values as map contain id and aggregation value.
	 *
	 * @param timeSeriesArrays timeseries arrays
	 * @param duration duration to calculate value
	 * @return map contain id and aggregation value
	 */
	private Map<String, BigDecimal> aggregationTimeSeries( TimeSeriesArrays timeSeriesArrays, int duration ){
		this.getLogger().log( LogLevel.INFO, "GridCorrectFeatureThresholdAdapter: try to calculate the timeseries duration values." );
		return TimeSeriesUtils.toList( timeSeriesArrays ).stream()
			.collect( Collectors.toMap(
					timeSeriesArray -> timeSeriesArray.getHeader().getLocationId(),
					timeSeriesArray -> Aggregations.accumulative( timeSeriesArray, timeSeriesArray.size() - duration, timeSeriesArray.size() )
			) );
	}

	/**
	 * Correct grids by aggregation values and features.
	 *
	 * @param inputNC input netcdf
	 * @param geometryMap map contain id and geometry
	 * @param aggregationMap map contain id and aggregation value
	 * @param threshold value of threshold
	 * @param parameter parameter
	 * @return corrected grids
	 * @throws IOException has IO exception
	 */
	private List<StandardGrid> correctGrids( Path inputNC,
			Map<String, Geometry> geometryMap, Map<String, BigDecimal> aggregationMap,
			BigDecimal threshold, String parameter ) throws IOException {
		this.getLogger().log( LogLevel.INFO,
				"GridCorrectFeatureThresholdAdapter: try to correct the grids: {} with features value and threshold: {}.", inputNC.toString(), threshold );
		try ( NetCDFReader reader = NetCDFReader.read( inputNC ) ){
			List<StandardGrid> grids = NetCDFGridMapper.fromTYXModel( reader, parameter );
			return grids.parallelStream()
				.peek( grid ->
					aggregationMap.forEach( (id, value) -> {
						if ( NumberUtils.less( value, threshold ) && geometryMap.containsKey( id ) ){
							this.getLogger().log( LogLevel.INFO,
									"GridCorrectFeatureThresholdAdapter: Grid got correct because features id: {}, value: {} less than threshold: {}.", id, value, threshold );
							Geometry geometry = geometryMap.get( id );
							grid.forEachGridIndexed( (index, c) -> {
								Point point = JTSUtils.point( c );
								if ( geometry.intersects( point ) ){
									grid.setValue( BigDecimal.ZERO, index.getCol(), index.getRow() );
								}
							},
							index -> grid.isNotMissing( index.getCol(), index.getRow() ) );
						} else {
							this.getLogger().log( LogLevel.INFO,
									"GridCorrectFeatureThresholdAdapter: Grid skip correct because features id: {}, value: {} greater than threshold: {}.", id, value, threshold );
						}
					} )
				)
				.collect( Collectors.toList() );
		}
	}
}
