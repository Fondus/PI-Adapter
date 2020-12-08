package tw.fondus.fews.adapter.pi.virtualiot.drwu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.virtualiot.drwu.argument.PostAdapterArguments;

/**
 * Unit test of virtual IoT model post-adapter.
 * 
 * @author Chao
 *
 */
public class VirtualIoTPostAdapterTest {
	@Before
	public void run() {
		String[] args = new String[] {
			"-b",
			"src/test/resources",
			"-i",
			"input.xml,OUTPUT_EST_FDEP_GRID_ANN_T1001.TXT,outputOrder.txt",
			"-o",
			"Output.xml",
			"-p",
			"Depth.simulated",
			"-u",
			"m"
		};
		
		PostAdapterArguments arguments = PostAdapterArguments.instance();
		new VirtualIoTPostAdapter().execute( args, arguments );
	}

	@Test
	public void check() {
		Assert.assertTrue(
				PathUtils.isExists( PathUtils.path( "src/test/resources/Output/Output.xml" ) ) );
	}
}
