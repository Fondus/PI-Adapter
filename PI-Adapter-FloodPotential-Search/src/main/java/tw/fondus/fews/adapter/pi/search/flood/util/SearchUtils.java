package tw.fondus.fews.adapter.pi.search.flood.util;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.fews.pi.util.transformation.Aggregations;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.string.Strings;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
}
