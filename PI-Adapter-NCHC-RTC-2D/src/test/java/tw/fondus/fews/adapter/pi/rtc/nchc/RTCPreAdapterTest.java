package tw.fondus.fews.adapter.pi.rtc.nchc;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.rtc.nchc.util.PreAdapterArguments;

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
				"Simulation.xml,Observation.xml",
				"-o",
				"",
				"-f",
				"3"
		};
		
		PreAdapterArguments arguments = new PreAdapterArguments();
		new RTCPreAdapter().execute( args, arguments );
	}
}
