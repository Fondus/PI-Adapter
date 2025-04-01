package tw.fondus.fews.adapter.pi.report.rmo07;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.ProcessArguments;

/**
 * The unit test of ExcelReportStatisticsProcess.
 *
 * @author Chao
 *
 */
public class ExcelReportProcessTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-is",
				"21",
				"-ie",
				"45",
				"-sc",
				"1730H065"
		};

		ProcessArguments arguments = ProcessArguments.instance();
		new ExcelReportProcess().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/四重溪_FEWS_2025021418_QPESUMS_QPF.xlsx" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/東港溪_FEWS_2025021418_QPESUMS_QPF.xlsx" ) ) );
		Assert.assertTrue( PathUtils.isExists( PathUtils.path( "src/test/resources/Output/高屏溪_FEWS_2025021418_QPESUMS_QPF.xlsx" ) ) );
	}
}
