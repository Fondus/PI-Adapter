package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model pre-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPreAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"Rainfall_2018100100.xml,Q_2018100100.xml",
				"-o",
				""
		};
		
		PiIOArguments arguments = new PiIOArguments();
		new LTFPreAdapter().execute( args, arguments );
	}
}
