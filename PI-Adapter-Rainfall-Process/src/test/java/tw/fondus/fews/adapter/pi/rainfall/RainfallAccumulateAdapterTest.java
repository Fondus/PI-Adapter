package tw.fondus.fews.adapter.pi.rainfall;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.rainfall.argument.IndexArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The unit test of RainfallAccumulateAdapter.
 *
 * @author Brad Chen
 *
 */
public class RainfallAccumulateAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml",
				"-o",
				"Output.xml",
				"-s",
				"16",
				"-si",
				"0",
				"-ei",
				"23"
		};

		IndexArguments arguments = IndexArguments.instance();
		new RainfallAccumulateAdapter().execute( args, arguments );
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void test() throws IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Output/Output.xml" ) );
		Assert.assertFalse( timeSeriesArrays.isEmpty() );
	}

	@After
	public void clear() throws IOException {
		Files.deleteIfExists( Paths.get( "src/test/resources/Output/Output.xml" ) );
	}
}
