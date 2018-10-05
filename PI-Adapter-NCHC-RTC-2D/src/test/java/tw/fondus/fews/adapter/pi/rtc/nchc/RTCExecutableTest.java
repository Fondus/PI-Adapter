package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.util.RunArguments;

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
				"INPUT_VAL_GRIDS_OBS_EST_T1,INPUT_VAL_GRIDS_OBS_EST.TXT,OUTPUT_SIM_WH.ASC,OUTPUT_ERR_SIM_WH.ASC,mapstacks.xml",
				"-o",
				"OUTPUT_EST_VAL_GRIDS_PARS.TXT,OUTPUT_EST_VAL_GRIDS_APP.TXT,OUTPUT_CORR_SIM_WH.TXT,OUTPUT_CORR_SIM_WH_1001.ASC",
				"-f",
				"4",
				"-ed",
				"2D_RTC_Part2",
				"-e",
				"PRO_EST_VL_GRIDS.exe,PRO_SET_SIMDH_RTSIMAP.exe",
				"-pn",
				"ANNAN"
		};
		
		RunArguments arguments = new RunArguments();
		new RTCExecutable().execute( args, arguments );
	}
}
