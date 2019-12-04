package tw.fondus.fews.adapter.pi.nc;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.nc.argument.RestructureArguments;

/**
 * The unit test of NetCDFReStructureAdapter.
 *
 * @author Brad Chen
 *
 */
public class GridRestructureAdapterTest {
	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-id",
				"Output/",
				"-i",
				"Flood.nc",
				"-o",
				"FloodNew.nc",
				"-p",
				"depth_below_surface_simulated",
				"-u",
				"m",
				"-vn",
				"depth_below_surface_simulate",
				"-tn",
				"time",
				"-yn",
				"lat",
				"-xn",
				"lon",
				"-to",
				"0",
				"-yo",
				"2",
				"-xo",
				"1",
				"-tzFlag",
				"-tz",
				"+0800"
		};

		RestructureArguments arguments = new RestructureArguments();
		new GridRestructureAdapter().execute( args, arguments );
	}
}
