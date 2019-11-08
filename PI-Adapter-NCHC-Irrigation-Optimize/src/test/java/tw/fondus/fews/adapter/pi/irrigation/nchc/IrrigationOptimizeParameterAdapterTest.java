package tw.fondus.fews.adapter.pi.irrigation.nchc;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.irrigation.nchc.argument.ParameterArguments;

/**
 * The unit test of IrrigationOptimizeParameterAdapter.
 *
 * @author Brad Chen
 *
 */
public class IrrigationOptimizeParameterAdapterTest {

	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-url",
				"",
				"-token",
				"",
				"-r",
				"Hsinchu_ZhuDong",
				"-c",
				"Case6",
				"-hs",
				"員崠,寶山",
				"-wrt",
				"竹東圳,樹杞林",
				"-wrf",
				"INPUT_QD_ZONE.TXT,INPUT_QD_BL_INP.TXT"
		};

		ParameterArguments arguments = new ParameterArguments();
		new IrrigationOptimizeParameterAdapter().execute( args, arguments );
	}
}