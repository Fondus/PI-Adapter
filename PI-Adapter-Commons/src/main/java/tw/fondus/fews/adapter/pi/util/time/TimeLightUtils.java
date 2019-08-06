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
	 * Transform DateTime to String using the specified zone.
	 * 
	 * @param time
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static String toString( DateTime time, String format, DateTimeZone timeZone ) {
		Preconditions.checkNotNull( time, "TimeUtils: time." );
		Preconditions.checkNotNull( format, "TimeUtils: format." );
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );
		return formatter.withZone( timeZone ).print( time );
	}

	/**
	 * Transform String to DateTime using the specified zone.
	 * 
	 * @param time
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static DateTime toDateTime( String time, String format, DateTimeZone timeZone ) {
		Preconditions.checkNotNull( time, "TimeUtils: time." );
		Preconditions.checkNotNull( format, "TimeUtils: format." );
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern( format );
		return formatter.withZone( timeZone ).parseDateTime( time );
	}
}
