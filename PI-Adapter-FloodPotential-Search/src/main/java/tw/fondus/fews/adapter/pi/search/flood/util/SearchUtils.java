package tw.fondus.fews.adapter.pi.search.flood.util;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.fews.pi.util.transformation.Aggregations;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The tool of search flood potential with feature value.
 *
 * @author Brad Chen
 *
 */
public class SearchUtils {
	private SearchUtils(){}

	/**
	 * Read the meta-info file as value collection.
	 *
	 * @param metaPath file contain meta-info
	 * @return meta-info levels of NetCDF.
	 */
	public static List<Integer> readMetaInfo( Path metaPath ) {
		return PathReader.readAllLines( metaPath ).stream()
				.map( line -> line.split( Strings.COMMA )[1] )
				.map( Integer::valueOf )
				.collect( Collectors.toList() );
	}

	/**
	 * Read the flood potential maps by level from file.
	 *
	 * @param netCDFPath file path of netCDF
	 * @param parameterPrefix prefix of parameter
	 * @param levels meta-info levels of NetCDF
	 * @return map contain flood potential map with level key
	 * @throws IOException has IO Exception
	 */
	public static Map<Integer, StandardGrid> readFloodPotentialMaps( Path netCDFPath,
			String parameterPrefix, List<Integer> levels ) throws IOException {
		Map<Integer, StandardGrid> map = CollectionUtils.emptyMapHash();
		try ( NetCDFReader reader = NetCDFReader.read( netCDFPath ) ){
			levels.forEach( level -> {
				String parameterId = parameterPrefix + "_r" + level;
				StandardGrid grid = NetCDFGridMapper.fromYXModel( reader, parameterId );
				map.putIfAbsent( level, grid );
			} );
		}
		return map;
	}

	/**
	 * Calculate the accumulated value of each array to map with location id key.
	 *
	 * @param timeSeriesArrays time-series arrays
	 * @return map contain time series accumulated value with location id key
	 */
	@SuppressWarnings( "rawtypes" )
	public static Map<String, BigDecimal> accumulate( TimeSeriesArrays timeSeriesArrays ) {
		return TimeSeriesUtils.toList( timeSeriesArrays )
				.stream()
				.collect( Collectors.toMap(
						TimeSeriesUtils.MAPPER_ARRAY_TO_LOCATION,
						array -> Aggregations.accumulative( array, 0, array.size() )
						) );
	}

	/**
	 * Find the value first cross level.
	 *
	 * @param value value
	 * @param levels meta-info levels of NetCDF
	 * @return cross level, it's optional
	 */
	public static Optional<Integer> findFirstCrossLevel( BigDecimal value, List<Integer> levels ) {
		return levels.stream()
				.filter( level -> NumberUtils.greater( value, NumberUtils.create( level ) ) )
				.findFirst();
	}
}
