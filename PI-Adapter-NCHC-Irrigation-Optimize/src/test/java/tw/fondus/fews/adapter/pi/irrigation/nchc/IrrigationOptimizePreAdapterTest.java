package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.PreArguments;

/**
 * The unit test of IrrigationOptimizePreAdapter.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizePreAdapterTest {

	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"WaterLevel_LongTime.xml",
				"-o",
				"INPUT_QIN_ZONE.TXT"
		};

		PreArguments arguments = PreArguments.instance();
		new IrrigationOptimizePreAdapter().execute( args, arguments );
	}
}
