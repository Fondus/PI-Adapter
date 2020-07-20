package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.PreAdapterArguments;

/**
 * Unit test of Model pre-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHPreAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml,Rainfall.txt",
				"-o",
				"Output.txt",
				"-gf",
				"Wagis.txt",
				"-a",
				"175.74",
				"-no",
				"6.5",
				"-nc",
				"0.03",
				"-w",
				"80",
				"-inf",
				"0.5"
		};
	
		PreAdapterArguments arguments = PreAdapterArguments.instance();
		new KWGIUHPreAdapter().execute( args, arguments );
	}
}
