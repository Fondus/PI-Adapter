package tw.fondus.fews.adapter.pi.annfsm.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.ExecutableArguments;

/**
 * Unit test of Model executable for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class ANNFSMExecutableTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"ANNSFM_inputs.txt,ANNSFM_data_size.txt",
				"-o",
				"",
				"-e",
				"ANNSFM.exe"
		};
	
		ExecutableArguments arguments = new ExecutableArguments();
		new ANNFSMExecutable().execute( args, arguments );
	}
}
