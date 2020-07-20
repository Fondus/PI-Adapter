package tw.fondus.fews.adapter.pi.util;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The unit test of TimeSeriesLight tools.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class TimeSeriesLightTest {
	private final Path root = Paths.get( "src/test/resources" );
	private final Path readPath = root.resolve( "Rainfall.xml" );
	private TimeSeriesArrays timeSeriesArrays;

	@Before
	public void testRead() throws IOException {
		this.timeSeriesArrays = TimeSeriesLightUtils.read( readPath );
		Assert.assertTrue( timeSeriesArrays.size() > 0 );
	}

	@Test
	public void testGetValue(){
		var value = TimeSeriesLightUtils.getValue( timeSeriesArrays.get( 0 ), 0 );
		Assert.assertEquals( -999.0, value.doubleValue(), 0 );
	}

	@Test
	public void testWrite() throws IOException {
		//  Prepare to test
		Path outputPath = root.resolve( "output.xml" );

		//  Test Write
		var handler = TimeSeriesLightUtils.seriesHandler();
		TimeSeriesLightUtils.addHeader( handler, "locationId", "parameterId", "unitId" );
		TimeSeriesLightUtils.addValue( handler, 1535068800000L, TimeSeriesLightUtils.MISSING_VALUE );

		Path actualOutput = TimeSeriesLightUtils.write( handler, outputPath );
		var arrays = TimeSeriesLightUtils.read( actualOutput );
		Assert.assertEquals( 1, arrays.size() );
		Assert.assertEquals( "locationId", arrays.get( 0 ).getHeader().getLocationId() );
		Assert.assertEquals( TimeSeriesLightUtils.MISSING_VALUE, TimeSeriesLightUtils.getValue( arrays.get( 0 ), 0 ) );

		//  Clean Test
		Files.deleteIfExists( outputPath );
	}
}
