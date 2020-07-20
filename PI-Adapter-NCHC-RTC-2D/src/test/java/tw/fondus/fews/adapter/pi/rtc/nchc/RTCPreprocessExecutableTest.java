package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.argument.RunArguments;

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
				"",
				"-o",
				"",
				"-f",
				"3",
				"-td",
				"Template/2D_RTC_Part1/",
				"-e",
				"PRO_SET_SIMDH_RTSIMAP.exe",
				"-pn",
				"ANNAN"
		};
		
		RunArguments arguments = RunArguments.instance();
		new RTCPreprocessExecutable().execute( args, arguments );
	}
}
