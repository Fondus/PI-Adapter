package tw.fondus.fews.adapter.pi.report.rmo07;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.CrossSectionChartArguments;

/**
 * The unit test of CrossSectionChartProcess.
 *
 * @author Chao
 *
 */
public class CrossSectionChartProcessTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-is",
				"0",
				"-ie",
				"33",
				"-w",
				"900",
				"-he",
				"300"
		};

		CrossSectionChartArguments arguments = CrossSectionChartArguments.instance();
		new CrossSectionChartProcess().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/1730H036.png" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/1740H002.png" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/1850H004.png" ) ) );
	}
}
