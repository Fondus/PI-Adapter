package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.PreAdapterArguments;

/**
 * Unit test of virtual IoT model pre-adapter.
 * 
 * @author Chao
 *
 */
public class VirtualIoTPreAdapterTest {
	@Before
	public void run() {
		String[] args = new String[] {
			"-b",
			"src/test/resources",
			"-i",
			"Input.xml,InputOrder.txt",
			"-o",
			"INPUT_OBS_FDEP_GAGE_OBS_T1001.TXT",
			"-ba",
			"Yilan"
		};
		
		PreAdapterArguments arguments = PreAdapterArguments.instance();
		new VirtualIoTPreAdapter().execute( args, arguments );
	}

	@Test
	public void check() {
		Assert.assertTrue(
				PathUtils.isExists( PathUtils.path( "src/test/resources/Input/INPUT_OBS_FDEP_GAGE_OBS_T1001.TXT" ) ) );
	}
}
