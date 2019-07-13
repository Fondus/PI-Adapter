package tw.fondus.fews.adapter.pi.grid.merge;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.grid.merge.util.RunArguments;

/**
 * Unit test of Model executable-adapter for running ESRI Grid ASCII merged model.
 * 
 * @author Brad Chen
 *
 */
public class MergeTest {

	@Test
	public void test() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-e",
				"GridMerge.exe",
				"-i",
				"UNIT01.xml,zonelist.dat,zone_id.asc,UNITTEMP.asc",
				"-o",
				"UNITALL.asc,UNITALL.xml,UNITALL????.asc",
				"-ed",
				"/Programs/",
				"-td",
				"/Temp/"
				};
		
		RunArguments arguments = new RunArguments();
		new GridMergeExecutable().execute( args, arguments );
	}
}
