package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.ExecutableArguments;

/**
 * Unit test of Model executable-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHExecutableTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.txt",
				"-o",
				"",
				"-e",
				"KWGIUH_for_FEWS.exe"
		};
	
		ExecutableArguments arguments = ExecutableArguments.instance();
		new KWGIUHExecutable().execute( args, arguments );
	}
}
