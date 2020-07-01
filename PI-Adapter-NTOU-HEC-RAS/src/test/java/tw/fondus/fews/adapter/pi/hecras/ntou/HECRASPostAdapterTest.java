package tw.fondus.fews.adapter.pi.hecras.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.hecras.ntou.argument.ProcessArguments;

/**
 * Unit test of Model Post-Adapter for running NTOU HEC-RAS model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class HECRASPostAdapterTest {
	@Test
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"TC_1.g01.hdf,TC_1.p03.hdf,WaterLevel.xml",
				"-o",
				"location.csv,Depth.csv,Flow.xml",
				"-c",
				"Toucian",
				"-p",
				"Depth.simulated"
		};
		
		ProcessArguments arguments = ProcessArguments.instance();
		new HECRASPostAdapter().execute( args, arguments );
	}
}
