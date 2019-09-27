package tw.fondus.fews.adapter.pi.annfsm.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.ExecutableArguments;

/**
 * Unit test of Model post-adapter for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class ANNFSMPostAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"ANNSFM_outputs.txt,Tide.xml",
				"-o",
				"Output.xml",
				"-e",
				""
		};
	
		ExecutableArguments arguments = new ExecutableArguments();
		new ANNFSMPostAdapter().execute( args, arguments );
	}
}
