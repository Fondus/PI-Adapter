package tw.fondus.fews.adapter.pi.nc;

import org.junit.Before;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.nc.argument.RenameByTimeValueArguments;

/**
 * The unit test of GridMergeAdapter.
 *
 * @author Brad Chen
 *
 */
public class RenameNetCDFByTimeValueAdapterTest {
	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"TYX.nc",
				"-o",
				"",
				"-ti",
				"0"
		};

		RenameByTimeValueArguments arguments = RenameByTimeValueArguments.instance();
		new RenameNetCDFByTimeValueAdapter().execute( args, arguments );
	}

	@Test
	public void test() {

	}
}
