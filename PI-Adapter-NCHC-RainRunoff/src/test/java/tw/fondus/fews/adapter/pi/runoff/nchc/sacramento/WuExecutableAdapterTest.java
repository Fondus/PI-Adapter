package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuExecutableAdapterTest {

	@Before
	public void run() {
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
		
		RunArguments arguments = RunArguments.instance();
		new WuSacramentoExecutable().execute(args, arguments);
	}

	@Test
	public void test(){
		Assert.assertTrue( PathUtils.list( "src/test/resources/Sacramento/Output" )
				.stream()
				.anyMatch( path -> PathUtils.equalsExtension( path, FileType.TXT ) ) );
	}
}
