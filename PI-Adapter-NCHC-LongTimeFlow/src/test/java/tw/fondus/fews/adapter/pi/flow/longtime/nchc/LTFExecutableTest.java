package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.flow.longtime.nchc.util.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFExecutableTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"",
				"-o",
				"",
				"-e",
				"PRO_EST_FLOW_ANN_GA-SA_MTF.exe"
		};
		
		RunArguments arguments = new RunArguments();
		new LTFExecutable().execute( args, arguments );
	}
}
