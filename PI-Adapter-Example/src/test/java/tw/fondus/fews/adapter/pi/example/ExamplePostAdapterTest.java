package tw.fondus.fews.adapter.pi.example;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The unit test of ExamplePostAdapter.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePostAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"TestLocation-output.txt",
				"-o",
				"Output.xml",
				"-p",
				"H.sim",
				"-u",
				"m"
				};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new ExamplePostAdapter().execute( args, arguments );
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void test() throws IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Output/Output.xml" ) );
		Assert.assertFalse( timeSeriesArrays.isEmpty() );

		TimeSeriesArray timeSeriesArray = timeSeriesArrays.get( 0 );
		Assert.assertEquals( 100.12971, timeSeriesArray.getValue( 0 ), 0.00001 );
		Assert.assertEquals( 100.71559, timeSeriesArray.getValue( 1 ), 0.00001 );

		TimeSeriesHeader header = timeSeriesArray.getHeader();
		Assert.assertEquals( "H.sim", header.getParameterId() );
		Assert.assertEquals( "m", header.getUnit() );
	}

	@After
	public void clear() throws IOException {
		Files.deleteIfExists( Paths.get( "src/test/resources/Output/Output.xml" ) );
	}
}
