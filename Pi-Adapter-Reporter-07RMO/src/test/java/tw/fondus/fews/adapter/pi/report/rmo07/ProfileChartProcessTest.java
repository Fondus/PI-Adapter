package tw.fondus.fews.adapter.pi.report.rmo07;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.CrossSectionChartArguments;

/**
 * The unit test of ProfileChartProcessTest.
 *
 * @author Chao
 *
 */
public class ProfileChartProcessTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-pf",
				"Profile_",
				"-is",
				"20",
				"-ie",
				"33",
				"-w",
				"1000",
				"-he",
				"400"
		};

		CrossSectionChartArguments arguments = CrossSectionChartArguments.instance();
		new ProfileChartProcess().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/Profile_四重溪_1850H004.png" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/Profile_東港溪_1740H002.png" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/Profile_高屏溪_1730H068.png" ) ) );
	}
}
