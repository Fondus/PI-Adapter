package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.ExecutableArguments;

/**
 * Unit test of virtual IoT model executable.
 * 
 * @author Chao
 *
 */
public class VirtualIoTExecutableTest {
	@Before
	public void run() {
		String[] args = new String[] {
			"-b",
			"src/test/resources",
			"-i",
			"INPUT_OBS_FDEP_GAGE_OBS_T1001.TXT",
			"-o",
			"",
			"-ba",
			"Ylian",
			"-e",
			"PRO_EST_FDEP_VIOT.exe"
		};
		
		ExecutableArguments arguments = ExecutableArguments.instance();
		new VirtualIoTExecutable().execute( args, arguments );
	}

	@Test
	public void check() {
		Assert.assertTrue(
				PathUtils.isExists( PathUtils.path( "src/test/resources/Work/OUTPUT_EST_FDEP_GRID_ANN_T1001.TXT" ) ) );
	}
}
