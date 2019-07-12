package tw.fondus.fews.adapter.pi.util;

import java.io.IOException;
import java.nio.file.Paths;

import javax.naming.OperationNotSupportedException;

import org.junit.Assert;
import org.junit.Test;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The unit test of TimeSeriesLight tools.
 * 
 * @author Brad Chen
 *
 */
public class TimeSeriesLightTest {
	
	@Test
	public void test() throws OperationNotSupportedException, IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( Paths.get( "src/test/resources/Rainfall.xml" ) );
		Assert.assertTrue( timeSeriesArrays.size() > 0 );
	}
}
