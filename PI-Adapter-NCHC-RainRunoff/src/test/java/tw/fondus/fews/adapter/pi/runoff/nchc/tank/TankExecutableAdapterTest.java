package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC Tank model.
 * 
 * @author Brad Chen
 *
 */
public class TankExecutableAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Tank",
				"-i",
				"INPUT_RAIN_TANK.txt,INPUT_PARS_TANK.TXT",
				"-o",
				"OUTPUT_FLOW_TANK.TXT",
				"-e",
				"est_flow_Tank.exe",
				"-ep",
				"Tank/",
				"-pp",
				"Parameters/"
				};
		
		RunArguments arguments = new RunArguments();
		new TankExecutable().execute(args, arguments);
	}
	
}
