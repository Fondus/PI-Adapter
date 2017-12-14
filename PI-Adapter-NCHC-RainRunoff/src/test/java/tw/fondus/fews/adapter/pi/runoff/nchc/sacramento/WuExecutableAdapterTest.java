package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuExecutableAdapterTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\RR_NCHC_Sacramento",
				"-i",
				"INPUT_DATA_RAIN.TXT,INPUT_PARS_SACSMA.TXT",
				"-o",
				"OUTPUT_EST_FLOW.TXT",
				"-e",
				"Est_Runoff__SACSMA_NCHC.exe",
				"-ep",
				"Work/",
				"-pp",
				"Parameters/"
				};
		
		RunArguments arguments = new RunArguments();
		new WuSacramentoExecutable().execute(args, arguments);
	}
	
}
