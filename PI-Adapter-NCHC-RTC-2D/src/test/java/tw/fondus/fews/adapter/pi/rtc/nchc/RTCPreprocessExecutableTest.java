package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.util.RunArguments;

/**
 * Unit test of Model preprocess executable-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCPreprocessExecutableTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"INPUT_WH_EST_OBS_GAUGES.txt,INPUT_CORR_SIM_WH.txt",
				"-o",
				"INPUT_VAL_GRIDS_OBS_EST_T1",
				"-f",
				"4",
				"-ed",
				"2D_RTC_Part1",
				"-e",
				"PRO_SET_SIMDH_RTSIMAP.exe",
				"-pn",
				"ANNAN"
		};
		
		RunArguments arguments = new RunArguments();
		new RTCPreprocessExecutable().execute( args, arguments );
	}
}
