package tw.fondus.fews.adapter.pi.trigrs;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.trigrs.util.PostArguments;

/**
 * Unit test of Model post-adapter for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
public class PostTRIGRSTest {
	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"\\TRIGRS\\bin",
				"-i",
				"map.xml",
				"-o",
				"TRIGRS",
				"-p",
				"Factor.safety",
				"-e",
				"47",
				"-id",
				"../Input/",
				"-od",
				"../Output/",
				"-ld",
				"../Diagnostics/"
				};
		
		PostArguments arguments = new PostArguments();
		new TRIGRSPostAdapter().execute(args, arguments);
	}
}
