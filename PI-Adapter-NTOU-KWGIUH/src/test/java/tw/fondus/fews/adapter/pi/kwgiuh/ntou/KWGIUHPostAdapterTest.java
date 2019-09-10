package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.ExecutableArguments;

/**
 * Unit test of Model post-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHPostAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml,Output.txt",
				"-o",
				"Output.xml",
				"-e",
				""
		};
	
		ExecutableArguments arguments = new ExecutableArguments();
		new KWGIUHPostAdapter().execute( args, arguments );
	}
}
