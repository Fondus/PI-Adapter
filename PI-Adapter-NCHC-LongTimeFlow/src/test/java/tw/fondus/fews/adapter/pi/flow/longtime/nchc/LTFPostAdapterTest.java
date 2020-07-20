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
				"Rainfall.xml,WaterLevel.xml,OUTPUT_EST_10-DAYS_RAIN_FLOW_ANN_GA-SA_MTF.TXT",
				"-o",
				"Rainfall.xml,WaterLevel.xml",
				"-p",
				"P.forecast,H.simulated"
		};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new LTFPostAdapter().execute( args, arguments );
	}
}
