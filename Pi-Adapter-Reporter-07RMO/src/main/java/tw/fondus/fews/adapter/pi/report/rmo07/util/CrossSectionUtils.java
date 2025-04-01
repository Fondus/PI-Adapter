package tw.fondus.fews.adapter.pi.report.rmo07.util;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;

import tw.fondus.commons.spatial.util.jts.JTSUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.CrossSection;

/**
 * The utils for cross section chart process.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class CrossSectionUtils {
	/**
	 * Read cross-sections data.
	 * 
	 * @param templateCrossSection path of cross-sections file.
	 * @return map of cross-sections
	 */
	public static Map<String, CrossSection> readCrossSection( Path templateCrossSection ) {
		return PathUtils.list( templateCrossSection ).stream().map( path -> {
			String locationId = PathUtils.getNameWithoutExtension( path );
			List<Coordinate> coordinates = PathReader.readAllLines( path )
					.stream()
					.skip( 4 )
					.filter( line -> !line.startsWith( "@END" ) )
					.filter( line -> !line.startsWith( "#" ) )
					.map( line -> line.split( Strings.SPLIT_SPACE_MULTIPLE ) )
					.map( temps -> JTSUtils.coordinate( Double.parseDouble( temps[0] ),
							Double.parseDouble( temps[1] ) ) )
					.collect( Collectors.toList() );

			return CrossSection.builder().id( locationId ).coordinates( coordinates ).build();
		} ).collect( Collectors.toMap( cs -> cs.getId(), cs -> cs ) );
	}
}
