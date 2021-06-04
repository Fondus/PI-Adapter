package tw.fondus.fews.adapter.pi.aws.storage.util;

import org.joda.time.DateTime;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.aws.storage.argument.S3Arguments;

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
	 * Create the full text of object.
	 *
	 * @param arguments arguments
	 * @param input input file
	 * @return full text of object
	 */
	public static String createObjectWithPrefix( S3Arguments arguments, Path input ){
		PredefinePrefix predefinePrefix = arguments.getPredefinePrefix();
		String object;
		switch ( predefinePrefix ) {
		case TIME_FROM_NAME_YMD_THREE_TIER:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameThreeTierYMD( input, false, false ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YMD_THREE_TIER_IOW:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameThreeTierYMD( input, false, true ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YMD_THREE_TIER_GMT8:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameThreeTierYMD( input, true, false ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YMD_THREE_TIER_GMT8_IOW:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameThreeTierYMD( input, true, true ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YM_TWO_TIER:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameTwoTierYM( input, false, false ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YM_TWO_TIER_IOW:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameTwoTierYM( input, false, true ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YM_TWO_TIER_GMT8:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameTwoTierYM( input, true, false ) + arguments.getObject();
			break;
		case TIME_FROM_NAME_YM_TWO_TIER_GMT8_IOW:
			object = arguments.getObjectPrefix() +
					PredefinePrefixUtils.fromFileNameTwoTierYM( input, true, true ) + arguments.getObject();
			break;
		default:
			object = arguments.getObjectPrefix() + arguments.getObject();
		}
		return object;
	}

	/**
	 * Create the three-tier YMD prefix.
	 *
	 * @param input input
	 * @param isGMT8 is GMT8
	 * @param isIoWFormat is IoW format
	 * @return prefix
	 */
	public static String fromFileNameThreeTierYMD( Path input, boolean isGMT8, boolean isIoWFormat ){
		String name = PathUtils.getNameWithoutExtension( input );
		DateTime time = isGMT8 ? JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm", JodaTimeUtils.UTC8 ) :
				JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm" );
		String year = String.valueOf( time.getYear() );
		String month = String.format( "%02d", time.getMonthOfYear() );
		String day = String.format( "%02d", time.getDayOfMonth() );
		Stream<String> stream = isIoWFormat ? Stream.of( "Y" + year, "M" + month, "D" + day ) :
				Stream.of( year, month, day );
		return stream.collect( Collectors.joining( Strings.SLASH ) );
	}

	/**
	 * Create the two-tier YM prefix.
	 *
	 * @param input input
	 * @param isGMT8 is GMT8
	 * @param isIoWFormat is IoW format
	 * @return prefix
	 */
	public static String fromFileNameTwoTierYM( Path input, boolean isGMT8, boolean isIoWFormat ){
		String name = PathUtils.getNameWithoutExtension( input );
		DateTime time = isGMT8 ? JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm", JodaTimeUtils.UTC8 ) :
				JodaTimeUtils.toDateTime( name, "yyyyMMddHHmm" );
		String year = String.valueOf( time.getYear() );
		String month = String.format( "%02d", time.getMonthOfYear() );
		Stream<String> stream = isIoWFormat ? Stream.of( "Y" + year, "M" + month ) :
				Stream.of( year, month );
		return stream.collect( Collectors.joining( Strings.SLASH, Strings.BLANK, Strings.SLASH ) );
	}
}
