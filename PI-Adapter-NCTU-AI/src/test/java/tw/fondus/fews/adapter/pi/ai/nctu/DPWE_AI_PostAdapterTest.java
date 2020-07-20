package tw.fondus.fews.adapter.pi.ai.nctu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.nio.file.Path;

/**
 * The unit test of DPWE AI Model adapter for post process.
 * 
 * @author Brad Chen
 *
 */
public class DPWE_AI_PostAdapterTest {
	private final String folderInput = "src/test/resources/NCTU-AI-DPWE/Input";
	private final String folderOutput = "src/test/resources/NCTU-AI-DPWE/Output";
	private final String folderBackup = "src/test/resources/NCTU-AI-DPWE/Backup";
	
	@Before
	public void setUp() {
		Path pathInput = PathUtils.path( folderInput );
		Path pathOutput = PathUtils.path( folderOutput );
		Path pathBackup = PathUtils.path( folderBackup );
		
		Assert.assertTrue( PathUtils.isExists( pathInput ) && PathUtils.isExists( pathOutput ) && PathUtils.isExists( pathBackup ) );
		
		PathUtils.clean( pathInput );
		PathUtils.clean( pathOutput );
		PathUtils.copies( pathBackup, pathInput );
	}

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/NCTU-AI-DPWE",
				"-i",
				"time.txt,T.zip,T+1.zip,T+2.zip,T+3.zip",
				"-o",
				"map.xml",
				"-p",
				"Depth.simulated"
				};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new DPWE_AI_PostAdapter().execute( args, arguments );
	}

}
