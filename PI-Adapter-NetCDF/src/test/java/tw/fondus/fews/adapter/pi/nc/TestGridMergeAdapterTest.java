package tw.fondus.fews.adapter.pi.nc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.io.IOException;

/**
 * The unit test of TestGridMergeAdapter.
 *
 * @author Brad Chen
 *
 */
public class TestGridMergeAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"",
				"-o",
				"Merged.nc",
				"-p",
				"precipitation_radar",
				"-u",
				"m"
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new TestGridMergeAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( PathUtils.path( "src/test/resources/Output/Merged.nc" ) ) ){
			reader.findVariable( "precipitation_radar" ).ifPresent( variable -> {

			} );
		}
	}

	@After
	public void clear() {
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Output/Merged.nc" ) );
	}
}
