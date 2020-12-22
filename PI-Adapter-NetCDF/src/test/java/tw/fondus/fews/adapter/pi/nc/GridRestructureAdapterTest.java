package tw.fondus.fews.adapter.pi.nc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.nc.argument.RestructureArguments;

import java.io.IOException;

/**
 * The unit test of NetCDFReStructureAdapter.
 *
 * @author Brad Chen
 *
 */
public class GridRestructureAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-id",
				"Output/",
				"-i",
				"Flood.nc",
				"-o",
				"FloodNew.nc",
				"-p",
				"depth_below_surface_simulated",
				"-u",
				"m",
				"-vn",
				"depth_simulate",
				"-tn",
				"time",
				"-yn",
				"lat",
				"-xn",
				"lon",
				"-to",
				"0",
				"-yo",
				"2",
				"-xo",
				"1",
				"-tzFlag",
				"-tz",
				"+0000"
		};

		RestructureArguments arguments = RestructureArguments.instance();
		new GridRestructureAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( PathUtils.path( "src/test/resources/Output/FloodNew.nc" ) ) ){
			Assert.assertTrue( reader.findVariable( "depth_below_surface_simulated" ).isPresent() );
			reader.findVariable( "depth_below_surface_simulated" ).ifPresent( variable -> {
				int[] shape = variable.getShape();
				Assert.assertEquals( 3, shape[0] );
				Assert.assertEquals( 838, shape[1] );
				Assert.assertEquals( 584, shape[2] );
			} );
		}
	}

	@After
	public void clear() {
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Output/FloodNew.nc" ) );
	}
}
