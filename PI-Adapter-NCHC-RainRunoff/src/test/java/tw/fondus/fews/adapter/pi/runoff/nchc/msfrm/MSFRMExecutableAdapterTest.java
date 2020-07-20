package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC MSFRM model.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMExecutableAdapterTest {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/MSFRM",
				"-i",
				"INPUT_DATA_RAIN_EV.TXT,INPUT_EST_FLOW_MSFRM.TXT",
				"-o",
				"OUTPUT_EST_FLOW_MSFRM.TXT",
				"-e",
				"pro_est_flow_msfrm.exe",
				"-ed",
				"Work/",
				"-pd",
				"Parameters/"
				};
		
		RunArguments arguments = RunArguments.instance();
		new MSFRMExecutable().execute(args, arguments);
	}

	@Test
	public void test(){
		Assert.assertTrue( PathUtils.list( "src/test/resources/MSFRM/Output" )
				.stream()
				.anyMatch( path -> PathUtils.equalsExtension( path, FileType.TXT ) ) );
	}
}
