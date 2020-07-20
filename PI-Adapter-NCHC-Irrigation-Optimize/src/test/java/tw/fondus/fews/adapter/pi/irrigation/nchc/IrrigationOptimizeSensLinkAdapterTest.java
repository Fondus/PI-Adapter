package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.SensLinkArgument;

/**
 * The unit test of IrrigationOptimizeSensLinkAdapter.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeSensLinkAdapterTest {

	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"1300H014",
				"-o",
				"WaterLevel_SensLink.xml",
				"-p",
				"H.simulated",
				"-u",
				"m",
				"-wrb",
				"",
				"-wrt",
				"",
				"-wrf",
				"INPUT_QD_ZONE.TXT,INPUT_QD_BL_INP.TXT",
				"-ids",
				"",
				"-us",
				"",
				"-pw",
				""
		};

		SensLinkArgument argument = SensLinkArgument.instance();
		new IrrigationOptimizeSensLinkAdapter().execute( args, argument );
	}
}
