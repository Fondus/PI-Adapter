package tw.fondus.fews.adapter.pi.nc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of TestGridMergeAdapter.
 *
 * @author Brad Chen
 *
 */
@Ignore
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
				"-t",
				"201902061800"
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new TestGridMergeAdapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assert.assertTrue( PathUtils.isExists( "src/test/resources/Output/Merged.nc" ) );
	}

	@After
	public void clear() {
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Output/Merged.nc" ) );
	}
}
