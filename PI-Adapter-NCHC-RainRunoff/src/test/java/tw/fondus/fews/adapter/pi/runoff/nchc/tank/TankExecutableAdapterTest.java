package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankExecutableAdapterTest {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/Tank",
				"-i",
				"INPUT_RAIN_TANK.txt,INPUT_PARS_TANK.TXT",
				"-o",
				"OUTPUT_FLOW_TANK.TXT",
				"-e",
				"est_flow_Tank.exe",
				"-ed",
				"Tank/",
				"-pd",
				"Parameters/"
				};
		
		RunArguments arguments = RunArguments.instance();
		new TankExecutable().execute(args, arguments);
	}

	@Test
	public void test(){
		Assert.assertTrue( PathUtils.list( "src/test/resources/Tank/Output" )
				.stream()
				.anyMatch( path -> PathUtils.equalsExtension( path, FileType.TXT ) ) );
	}
}
