package tw.fondus.fews.adapter.pi.argument.converter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.beust.jcommander.IStringConverter;

/**
 * Convert arguments string to joda time.
 * 
 * @author Brad Chen
 *
 */
public class DateTimeConverter implements IStringConverter<DateTime> {
	private static final String FORMAT = "yyyyMMddHHmm";
	
	@Override
	public DateTime convert( String value ) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern( FORMAT );
		return formatter.withZone( DateTimeZone.UTC ).parseDateTime( value );
	}
}
