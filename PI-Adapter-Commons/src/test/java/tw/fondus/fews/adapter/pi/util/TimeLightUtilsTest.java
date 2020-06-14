package tw.fondus.fews.adapter.pi.util;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;

/**
 * The unit test of TimeLightUtils.
 *
 * @author Brad Chen
 *
 */
public class TimeLightUtilsTest {
	@Test
	public void testToString(){
		DateTime dateTime = new DateTime( TimeLightUtils.UTC0 ).withDate( 2018, 8, 20 ).withTime( 16, 0, 0, 0 );
		Assert.assertEquals( "201808201600", TimeLightUtils.toString( dateTime, "yyyyMMddHHmm" ) );
	}

	@Test
	public void testToDateTime(){
		DateTime dateTime = TimeLightUtils.toDateTime( "201808201600", "yyyyMMddHHmm" );
		Assert.assertTrue( dateTime.isBeforeNow() );
	}
}
