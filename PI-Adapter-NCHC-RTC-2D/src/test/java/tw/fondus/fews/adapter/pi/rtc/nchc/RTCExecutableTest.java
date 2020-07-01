package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.argument.RunArguments;

/**
 * Unit test of Model executable-adapter for running NCHC RTC model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class RTCExecutableTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"mapstacks.xml,97Annan_20mDEM.ASC",
				"-o",
				"",
				"-f",
				"3",
				"-td",
				"Template/2D_RTC_Part2",
				"-e",
				"PRO_EST_VL_GRIDS.exe,PRO_SET_SIMDH_RTSIMAP.exe",
				"-pn",
				"ANNAN"
		};
		
		RunArguments arguments = RunArguments.instance();
		new RTCExecutable().execute( args, arguments );
	}
}
