package tw.fondus.fews.adapter.pi.hecras.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.hecras.ntou.argument.ProcessArguments;

/**
 * Unit test of Model Pre-Adapter for running NTOU HEC-RAS model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class HECRASPreAdapterTest {
	@Test
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Flow.xml,Tide.xml,Left_Rainfall.xml,Right_Rainfall.xml",
				"-o",
				"TC_1.p03,TC_1.b03,TC_1.u01",
				"-c",
				"Toucian"
		};
		
		ProcessArguments arguments = new ProcessArguments();
		new HECRASPreAdapter().execute( args, arguments );
	}
}
