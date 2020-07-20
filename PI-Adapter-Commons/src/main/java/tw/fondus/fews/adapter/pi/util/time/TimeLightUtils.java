package tw.fondus.fews.adapter.pi.util.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Preconditions;

/**
 * A light version of FondUS SDK time tools, <br/>
 * it's used to transform JODA DateTime. 
 * 
 * @author Brad Chen
 *
 */
public class TimeLightUtils {
	/**
	 * The UTC0 time zone of the Joda DateTime.
	 */
	public static final DateTimeZone UTC0 = DateTimeZone.UTC;

	/**
	 * The UTC8 time zone of the Joda DateTime.
	 */
	public static final DateTimeZone UTC8 = DateTimeZone.forOffsetHours( 8 );
	
	private TimeLightUtils() {}

	/**
	 * Transform joda time to string using the with default time zone.
	 *
	 * @param time joda time
	 * @param format format
	 * @return time string
	 * @since 3.0.0
	 */
	public static String toString( DateTime time, String format ) {
		return toString( time, format, UTC0 );
	}

	/**
	 * Transform joda time to string using the specified time zone.
	 * 
	 * @param time joda time
	 * @param format format
	 * @param timeZone time zone
	 * @return time string
	 */
	public static String toString( DateTime time, String format, DateTimeZone timeZone ) {
		Preconditions.checkNotNull( time, "TimeUtils: time." );
		Preconditions.checkNotNull( format, "TimeUtils: format." );
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );
		return formatter.withZone( timeZone ).print( time );
	}

	/**
	 * Transform string to joda time using the with default time zone.
	 *
	 * @param time time string
	 * @param format format
	 * @return joda time
	 * @since 3.0.0
	 */
	public static DateTime toDateTime( String time, String format ) {
		return toDateTime( time, format, UTC0 );
	}

	/**
	 * Transform string to joda time using the specified time zone.
	 * 
	 * @param time time string
	 * @param format format
	 * @param timeZone time zone
	 * @return joda time
	 */
	public static DateTime toDateTime( String time, String format, DateTimeZone timeZone ) {
		Preconditions.checkNotNull( time, "TimeUtils: time." );
		Preconditions.checkNotNull( format, "TimeUtils: format." );
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );
		return formatter.withZone( timeZone ).parseDateTime( time );
	}
}
