package tw.fondus.fews.adapter.pi.senslink.v3;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;

import java.io.IOException;

/**
 * Unit test of Model adapter for import data from the SensLink 3.0.
 * 
 * @author Brad Chen
 *
 */
public class ImportFromSensLinkAdapterTest {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-t",
				"201910021600",
				"-ti",
				"0",
				"-d",
				"1",
				"-i",
				"ImportV3.xml",
				"-o",
				"OutputV3.xml",
				"-p",
				"H.obs",
				"-u",
				"m",
				"-us",
				"",
				"-pw",
				""
				};
		
		RunArguments arguments = RunArguments.instance();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void test() throws IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.read( "src/test/resources/Output/OutputV3.xml" );
		Assert.assertFalse( timeSeriesArrays.isEmpty() );

		TimeSeriesUtils.toList( timeSeriesArrays ).forEach( timeSeriesArray -> Assert.assertFalse( timeSeriesArray.isEmpty() ) );
	}

	@After
	public void clean(){
		PathUtils.deleteIfExists( "src/test/resources/Output/OutputV3.xml" );
	}
}
