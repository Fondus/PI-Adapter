package tw.fondus.fews.adapter.pi.report.rmo07.util;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.meta.WaterlevelMetaInfo;

/**
 * The utils for read waterlevel station meta info.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class WaterStationMetaUtils {
	/**
	 * Read watherlevel station meta info.
	 * 
	 * @param waterlevelPath path of waterlevel CSV file.
	 * @return map of waterlevel meta info
	 */
	public static Map<String, WaterlevelMetaInfo> readWaterlevelMetaInfo( Path waterlevelPath, Path attributePath ) {
		Map<String, WaterlevelMetaInfo> metaInfos = PathReader.readAllLines( waterlevelPath )
				.stream()
				.skip( 1 )
				.map( line -> line.split( Strings.COMMA, -1 ) )
				.collect( Collectors.toMap( split -> split[0],
						split -> WaterlevelMetaInfo.builder()
								.id( split[0] )
								.name( split[1] )
								.warningLevel1( createNumber( split[15] ) )
								.warningLevel2( createNumber( split[16] ) )
								.warningLevel3( createNumber( split[17] ) )
								.topLevel( createNumber( split[18] ) )
								.branch( split[10] )
								.town( split[12] )
								.crossSectionId( Strings.EMPTY )
								.build() ) );

		PathReader.readAllLines( attributePath ).stream().skip( 1 ).forEach( line -> {
			String[] temps = line.split( Strings.COMMA );
			String id = temps[0];
			if ( metaInfos.containsKey( id ) ) {
				WaterlevelMetaInfo metaInfo = metaInfos.get( id );
				metaInfo.setBranch( temps[2] );
				metaInfo.setTown( temps[3] );
				metaInfo.setCrossSectionId( temps[4] );
			}
		} );

		return metaInfos;
	}

	/**
	 * Create number by string(if blank return missing value)
	 * 
	 * @param numberString
	 * @return
	 */
	private static BigDecimal createNumber( String numberString ) {
		if ( StringUtils.isBlank( numberString ) ) {
			return Numbers.MISSING;
		} else {
			return NumberUtils.create( numberString );
		}
	}

	/**
	 * Search meta info by branch.
	 * 
	 * @param metaInfos list of meta info
	 * @param branch search branch name
	 * @return list of meta info include branch name
	 */
	public static List<WaterlevelMetaInfo> searchMetaInfosByBranch( Map<String, WaterlevelMetaInfo> metaInfos,
			String branch ) {
		return metaInfos.values()
				.stream()
				.filter( info -> StringUtils.isNotBlank( info.getBranch() )
						&& StringUtils.isNotBlank( info.getCrossSectionId() ) )
				.filter( info -> info.getBranch().equals( branch ) )
				.collect( Collectors.toList() );
	}
}
