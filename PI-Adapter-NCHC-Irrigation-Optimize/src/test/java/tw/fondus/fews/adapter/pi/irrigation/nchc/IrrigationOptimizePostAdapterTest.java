package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.PostArguments;

/**
 * The unit test of IrrigationOptimizePostAdapter.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizePostAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"OUTPUT_WS_EST_BL_OPT.TXT,WaterLevel_LongTime.xml",
				"-o",
				"Output.xml",
				"-p",
				"Q.simulated",
				"-u",
				"m3/s",
				"-d",
				"86400000"
		};

		PostArguments arguments = new PostArguments();
		new IrrigationOptimizePostAdapter().execute( args, arguments );
	}
}