package tw.fondus.fews.adapter.pi.nc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.io.IOException;

/**
 * The unit test of GridXYToLatLonAdapter.
 *
 * @author Brad Chen
 *
 */
public class GridXYToLatLonAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-od",
				"Input/",
				"-i",
				"TYX.nc",
				"-o",
				"TLatLon.nc",
				"-p",
				"precipitation_observed"
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new GridXYToLatLonAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( PathUtils.path( "src/test/resources/Input/TLatLon.nc" ) ) ){
			Assert.assertTrue( reader.findVariable( VariableName.LAT ).isPresent() );
			Assert.assertTrue( reader.findVariable( VariableName.LON ).isPresent() );
			Assert.assertTrue( reader.findVariable( "precipitation_observed" ).isPresent() );
			reader.findVariable( "precipitation_observed" ).ifPresent( variable -> {
				int[] shape = variable.getShape();
				Assert.assertEquals( 8, shape[0] );
				Assert.assertEquals( 61, shape[1] );
				Assert.assertEquals( 54, shape[2] );
			} );
		}
	}

	@After
	public void clear() {
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Input/TLatLon.nc" ) );
	}
}
