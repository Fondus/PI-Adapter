package tw.fondus.fews.adapter.pi.search.flood;

import com.google.common.base.Preconditions;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.geojson.model.feature.FeatureCollection;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.nc.util.TimeFactor;
import tw.fondus.commons.nc.util.key.VariableAttribute;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.crs.EPSG;
import tw.fondus.commons.spatial.util.geojson.GeoJsonMapper;
import tw.fondus.commons.spatial.util.jts.JTSUtils;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.StringFormatter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.zone.AreaTaiwan;
import tw.fondus.commons.util.zone.ZoneCounty;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.search.flood.argument.FloodPotentialSearchArguments;
import tw.fondus.fews.adapter.pi.search.flood.util.AccumulatedRainfallDuration;
import tw.fondus.fews.adapter.pi.search.flood.util.SearchUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter for running search flood potential with feature value wth Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class FloodPotentialSearchAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		FloodPotentialSearchArguments arguments = FloodPotentialSearchArguments.instance();
		new FloodPotentialSearchAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		FloodPotentialSearchArguments modelArguments = this.asArguments( arguments, FloodPotentialSearchArguments.class );

		Preconditions.checkState( modelArguments.getInputs().size() == 1,
				"FloodPotentialSearchAdapter: The input.xml not give by command." );
		Preconditions.checkState( modelArguments.getOutputs().size() == 1,
				"FloodPotentialSearchAdapter: The output not give by command." );

		// Prepare the file
		logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: The prepare phase with check inputs by command-line." );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"FloodPotentialSearchAdapter: The input.xml do not exists!" );

		Path featureFolder = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getFeaturePath() ),
				"FloodPotentialSearchAdapter: The features folder do not exists!" );

		Path featureFile = Prevalidated.checkExists(
				featureFolder.resolve( modelArguments.getFeatureFile() ),
				"FloodPotentialSearchAdapter: The features file do not exists in features folder." );

		Path databaseFolder = Prevalidated.checkExists(
				basePath.resolve( modelArguments.getDatabasePath() ),
				"FloodPotentialSearchAdapter: The database folder do not exists!" );

		AreaTaiwan area = Objects.requireNonNull( modelArguments.getArea(), "FloodPotentialSearchAdapter: The area not give by command." );
		ZoneCounty zone = Objects.requireNonNull( modelArguments.getZone(), "FloodPotentialSearchAdapter: The zone not give by command." );
		AccumulatedRainfallDuration duration = modelArguments.getAccumulatedRainfallDuration();

		Path targetFolder = Prevalidated.checkExists(
				databaseFolder.resolve( area.value() ).resolve( zone.value() ),
				"FloodPotentialSearchAdapter: The target database folder do not exists!" );

		String fileNameWithoutExtension = zone.value() + "_FloodPotential" + duration.getFileNameRule();

		Path targetMetaPath = Prevalidated.checkExists(
				targetFolder.resolve( fileNameWithoutExtension + FileType.TXT.getExtension() ),
				"FloodPotentialSearchAdapter: The target meta-info file do not exists!" );

		Path targetNetCDFPath = Prevalidated.checkExists(
				targetFolder.resolve( fileNameWithoutExtension + FileType.NETCDF.getExtension() ),
				"FloodPotentialSearchAdapter: The target netcdf file do not exists!" );

		logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: Start the search adapter process with Area: {}, Zone: {}, Accumulated duration: {}.",
				area.value(), zone.value(), duration.getName() );

		// Read Meta-Info
		logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: Read the level meta-info from file: {}.", targetMetaPath );
		List<Integer> levels = SearchUtils.readMetaInfo( targetMetaPath );
		if ( levels.isEmpty() ){
			String message = StringFormatter.format( "FloodPotentialSearchAdapter: The level meta-info from file: {} is empty.", targetMetaPath );
			logger.log( LogLevel.ERROR, message );
			throw new IllegalStateException( message );
		}

		// Read Geometry Features
		Map<String, Geometry> geometryMap = this.readFeatures( featureFile );
		if ( geometryMap.isEmpty() ){
			String message = StringFormatter.format( "FloodPotentialSearchAdapter: Not contain any features with file: {}", featureFile );
			logger.log( LogLevel.ERROR, message );
			throw new IllegalStateException( message );
		}

		try {
			// Read TimeSeries
			logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: Read the time-series from file: {} and calculate the accumulated value of each array.", inputXML );
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.read( inputXML );
			Map<String, BigDecimal> accumulateValues = SearchUtils.accumulate( timeSeriesArrays );

			// Read Flood Potential Maps
			logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: Read the flood potential maps from file: {}.", targetNetCDFPath );
			Map<Integer, StandardGrid> grids = SearchUtils.readFloodPotentialMaps( targetNetCDFPath, modelArguments.getParameter(), levels );
			if ( grids.isEmpty() ){
				String message = StringFormatter.format( "FloodPotentialSearchAdapter: Failed to read any flood potential map from file: {}", targetNetCDFPath );
				logger.log( LogLevel.ERROR, message );
				throw new IllegalStateException( message );
			}

			// Use Accumulate Values to get Flood Potential Map
			StandardGrid baseGrid = grids.get( levels.get( 0 ) )
					.clone( true )
					.calculate( value -> Numbers.ZERO );
			baseGrid.setProperty( VariableAttribute.KEY_NAME, modelArguments.getParameter() );
			baseGrid.setProperty( VariableName.TIME, modelArguments.getTimeZero().getMillis() );
			baseGrid.setProperty( "time-factor", TimeFactor.ARCHIVE );
			baseGrid.setProperty( "time-unit", "minutes since 1970-01-01 00:00:00.0 +0000" );

			accumulateValues.forEach( ( locationId, value ) -> {
				Optional<Integer> optionalLevel = SearchUtils.findFirstCrossLevel( value, levels );
				if ( geometryMap.containsKey( locationId ) && optionalLevel.isPresent() ){
					Integer level = optionalLevel.get();
					logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: location: {} with accumulated rainfall: {} across level: {}, start the search process.",
							locationId, value, level );

					Geometry geometry = geometryMap.get( locationId );
					StandardGrid target = grids.get( level );
					target.forEachGridIndexed( (index, c) -> {
						Point point = JTSUtils.point( c );
						if ( geometry.intersects( point ) ){
							int col = index.getCol();
							int row = index.getRow();
							baseGrid.setValue( target.value( col, row ), col, row );
						}
					}, index -> target.isNotMissing( index.getCol(), index.getRow() ) );

				} else {
					logger.log( LogLevel.WARN, "FloodPotentialSearchAdapter: location: {} with accumulated rainfall: {} not has feature or not across level, skip process.",
							locationId, value );
				}
			} );

			Path output = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
			logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: Write search result into file: {}.", output );
			NetCDFGridMapper.toTYXModel( output, fileNameWithoutExtension, CollectionUtils.toList( baseGrid ), EPSG.TWD97_TM2_121, false );

		} catch (IOException | InvalidRangeException e) {
			logger.log( LogLevel.ERROR, "FloodPotentialSearchAdapter: No time series found in file in the model input files!" );
		}

		logger.log( LogLevel.INFO, "FloodPotentialSearchAdapter: End the search adapter process with Area: {}, Zone: {}, Accumulated duration: {}.",
				area.value(), zone.value(), duration.getName() );
	}

	/**
	 * Read the features file as map contain id and geometry.
	 *
	 * @param featureFile file contain the features
	 * @return map contain id and geometry
	 */
	private Map<String, Geometry> readFeatures( Path featureFile ){
		this.getLogger().log( LogLevel.INFO, "FloodPotentialSearchAdapter: Read the features from file: {}.", featureFile );
		FeatureCollection collection = GsonMapperRuntime.GEOJSON.toBean( PathReader.readString( featureFile ), FeatureCollection.class );
		return collection.stream()
				.collect( Collectors.toMap(
						feature -> feature.getPropertyAsString( Strings.ID.toUpperCase() ),
						feature -> JTSUtils.transformCRS( GeoJsonMapper.toJTS( feature.getGeometry() ), EPSG.WGS84, EPSG.TWD97_TM2_121 )
				));
	}
}
