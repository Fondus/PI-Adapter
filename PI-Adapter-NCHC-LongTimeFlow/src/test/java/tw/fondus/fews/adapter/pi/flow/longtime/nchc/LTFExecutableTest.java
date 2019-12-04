package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.flow.longtime.nchc.argument.RunArguments;

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
				"DATA_INP_RAIN.txt,DATA_INP_WL.txt,INPUT_DATE_DECADE.TXT,INPUT_EST_FLOW_ANN_GA-SA_MTF.TXT",
				"-o",
				"OUTPUT_EST_10-DAYS_RAIN_FLOW_ANN_GA-SA_MTF.TXT",
				"-e",
				"PRO_EST_FLOW_ANN_GA-SA_MTF.exe",
				"-pn",
				"ShangPing"
		};
		
		RunArguments arguments = new RunArguments();
		new LTFExecutable().execute( args, arguments );
	}
}
