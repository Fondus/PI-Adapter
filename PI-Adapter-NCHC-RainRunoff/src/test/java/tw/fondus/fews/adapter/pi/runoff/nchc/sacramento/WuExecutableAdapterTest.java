package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuExecutableAdapterTest {

//	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/Sacramento",
				"-i",
				"INPUT_DATA_RAIN.TXT,INPUT_PARS_SACSMA.TXT",
				"-o",
				"OUTPUT_EST_FLOW.TXT",
				"-e",
				"Est_Runoff__SACSMA_NCHC.exe",
				"-ed",
				"Work/",
				"-pd",
				"Parameters/"
				};
		
		RunArguments arguments = new RunArguments();
		new WuSacramentoExecutable().execute(args, arguments);
	}
}
