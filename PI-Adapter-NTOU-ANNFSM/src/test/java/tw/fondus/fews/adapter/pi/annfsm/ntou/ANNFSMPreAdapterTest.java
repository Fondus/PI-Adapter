package tw.fondus.fews.adapter.pi.annfsm.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.PreAdapterArguments;

/**
 * Unit test of Model pre-adapter for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class ANNFSMPreAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Tide.xml",
				"-o",
				"ANNSFM_inputs.txt,ANNSFM_data_size.txt",
				"-c",
				"121.0142,24.827"
		};
	
		PreAdapterArguments arguments = new PreAdapterArguments();
		new ANNFSMPreAdapter().execute( args, arguments );
	}
}
