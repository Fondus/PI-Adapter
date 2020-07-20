package tw.fondus.fews.adapter.pi.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.test.argument.IndexArguments;

/**
 * The unit test of ForwardTimeContentByIndexAdapter.
 *
 * @author Brad Chen
 *
 */
public class ForwardTimeContentByIndexAdapterTest {
	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml,MetaInfo.dat",
				"-o",
				"Historical.xml,Forecast.xml",
				"-length",
				"24"
		};

		IndexArguments arguments = IndexArguments.instance();
		new ForwardTimeContentByIndexAdapter().execute( args, arguments );
	}

	@Test
	public void test(){
		Assert.assertTrue( PathUtils.isExists( "src/test/resources/MetaInfo.dat" ) );
		Assert.assertTrue( PathUtils.isExists( "src/test/resources/Output/Historical.xml" ) );
		Assert.assertTrue( PathUtils.isExists( "src/test/resources/Output/Forecast.xml" ) );
	}

	@After
	public void clean(){
		PathUtils.deleteIfExists( "src/test/resources/MetaInfo.dat" );
		PathUtils.deleteIfExists( "src/test/resources/Output/Historical.xml" );
		PathUtils.deleteIfExists( "src/test/resources/Output/Forecast.xml" );
	}
}
