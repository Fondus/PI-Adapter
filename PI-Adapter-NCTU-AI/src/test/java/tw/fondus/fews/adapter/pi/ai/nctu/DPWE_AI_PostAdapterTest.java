package tw.fondus.fews.adapter.pi.ai.nctu;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.util.file.PathUtils;

/**
 * The unit test of DPWE AI Model adapter for post process.
 * 
 * @author Brad Chen
 *
 */
public class DPWE_AI_PostAdapterTest {
	private String folderInput = "src/test/resources/NCTU-AI-DPWE/Input";
	private String folderOutput = "src/test/resources/NCTU-AI-DPWE/Output";
	private String folderBackup = "src/test/resources/NCTU-AI-DPWE/Backup";
	
	@Before
	public void setUp() throws Exception {
		Path pathInput = PathUtils.get( folderInput );
		Path pathOutput = PathUtils.get( folderOutput );
		Path pathBackup = PathUtils.get( folderBackup );
		
		Assert.assertTrue( PathUtils.exists( pathInput ) && PathUtils.exists( pathOutput ) && PathUtils.exists( pathBackup ) );
		
		PathUtils.deleteDirectory( pathInput, true );
		PathUtils.deleteDirectory( pathOutput, true );
		PathUtils.copyDirectory( pathBackup, pathInput, true );
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
		
		PiArguments arguments = new PiArguments();
		new DPWE_AI_PostAdapter().execute( args, arguments );
	}

}
