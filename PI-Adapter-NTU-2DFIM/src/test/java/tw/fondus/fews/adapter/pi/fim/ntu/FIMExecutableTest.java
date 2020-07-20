package tw.fondus.fews.adapter.pi.fim.ntu;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.fim.ntu.argument.RunArguments;

/**
 * Unit test of Model executable for running NTU 2DFIM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class FIMExecutableTest {
	@Test
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"QPESUMS.nc,QPESUMS.txt",
				"-o",
				"Depth.nc",
				"-f",
				"6",
				"-e",
				"run_nchc.bat"
		};
		
		RunArguments arguments = RunArguments.instance();
		new FIMExecutable().execute( args, arguments );
	}
}
