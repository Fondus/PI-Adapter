package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model post-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPostAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"Q_2018100100.xml",
				"-o",
				"output.xml"
		};
		
		PiIOArguments arguments = new PiIOArguments();
		new LTFPostAdapter().execute( args, arguments );
	}
}
