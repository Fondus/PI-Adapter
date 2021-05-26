package tw.fondus.fews.adapter.pi.aws.storage.util;

import org.joda.time.DateTime;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The tools of predefine prefix.
 *
 * @author Brad Chen
 *
 */
public class PredefinePrefixUtils {
	private PredefinePrefixUtils(){}

	/**
	 * Create the three-tier DMY prefix.
	 *
	 * @param input input
	 * @param isGMT8 is GMT8
	 * @param isIoWFormat is IoW format
	 * @return prefix
	 */
	public static String fromFileNameThreeTierDMY( Path input, boolean isGMT8, boolean isIoWFormat ){
		String name = PathUtils.getNameWithoutExtension( input );
		DateTime time = isGMT8 ? JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm", JodaTimeUtils.UTC8 ) :
				JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm" );
		String year = String.valueOf( time.getYear() );
		String month = String.valueOf( time.getMonthOfYear() );
		String day = String.valueOf( time.getDayOfMonth() );
		Stream<String> stream = isIoWFormat ? Stream.of( "Y" + year, "M" + month, "D" + day ) :
				Stream.of( year, month, day );
		return stream.collect( Collectors.joining( Strings.SLASH ) );
	}
}
