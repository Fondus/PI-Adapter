package tw.fondus.fews.adapter.pi.nc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.nc.argument.MergeArguments;

import java.io.IOException;

/**
 * The unit test of GridMergeAdapter.
 *
 * @author Brad Chen
 *
 */
public class GridMergeAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-id",
				"Merge/",
				"-od",
				"Merge/",
				"-i",
				"",
				"-o",
				"Merged.nc",
				"-p",
				"depth_below_surface_simulated",
				"-c",
				"4",
				"-tr",
				"3"
		};

		MergeArguments arguments = MergeArguments.instance();
		new GridMergeAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( PathUtils.path( "src/test/resources/Merge/Merged.nc" ) ) ){
			Assert.assertTrue( reader.findVariable( "depth_below_surface_simulated" ).isPresent() );
			Assert.assertEquals( 12, reader.findTimes().size() );
		}
	}

	@After
	public void clear() {
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Merge/Merged.nc" ) );
	}
}
