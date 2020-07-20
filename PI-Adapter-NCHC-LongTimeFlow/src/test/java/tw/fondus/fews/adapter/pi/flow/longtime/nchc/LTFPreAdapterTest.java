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
				"Rainfall.xml,WaterLevel.xml",
				"-o",
				"DATA_INP_RAIN.txt,DATA_INP_WL.txt,INPUT_DATE_DECADE.TXT"
		};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new LTFPreAdapter().execute( args, arguments );
	}
}
