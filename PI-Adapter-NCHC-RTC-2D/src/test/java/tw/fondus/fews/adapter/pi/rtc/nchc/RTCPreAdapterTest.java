package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.util.PreArguments;

/**
 * Unit test of Model pre-adapter for running NCHC RTC model. 
 * 
 * @author Chao
 *
 */
public class RTCPreAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"Similation.xml,Observation.xml",
				"-o",
				"INPUT_CORR_SIM_WH.TXT,INPUT_WH_EST_OBS_GAUGES.TXT",
				"-f",
				"4"
		};
		
		PreArguments arguments = new PreArguments();
		new RTCPreAdapter().execute( args, arguments );
	}
}
