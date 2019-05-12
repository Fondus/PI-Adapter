package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

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
		
		PiArguments arguments = new PiArguments();
		new LTFPostAdapter().execute( args, arguments );
	}
}
