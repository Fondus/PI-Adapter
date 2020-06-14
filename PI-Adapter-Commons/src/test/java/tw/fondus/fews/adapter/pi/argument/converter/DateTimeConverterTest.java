package tw.fondus.fews.adapter.pi.argument.converter;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * The unit test of  DateTimeConverter.
 *
 * @author Brad Chen
 *
 */
public class DateTimeConverterTest {
	@Test
	public void test(){
		DateTime dateTime = new DateTimeConverter().convert( "201902061800" );
		Assert.assertEquals( 2019, dateTime.getYear() );
		Assert.assertEquals( 2, dateTime.getMonthOfYear() );
		Assert.assertEquals( 6, dateTime.getDayOfMonth() );
		Assert.assertEquals( 18, dateTime.getHourOfDay() );
	}
}
