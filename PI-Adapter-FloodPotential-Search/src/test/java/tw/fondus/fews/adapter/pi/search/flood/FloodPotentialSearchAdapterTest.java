package tw.fondus.fews.adapter.pi.search.flood;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.search.flood.argument.FloodPotentialSearchArguments;

/**
 * The unit test of FloodPotentialSearchAdapter.
 *
 * @author Brad Chen
 *
 */
public class FloodPotentialSearchAdapterTest {
	@BeforeAll
	public static void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Rainfall.xml",
				"-o",
				"Output.nc",
				"-ff",
				"Towns.geojson",
				"-p",
				"depth_below_surface_simulated",
				"-ai",
				"Southern",
				"-zi",
				"Pingtung",
				"-ad",
				"06h",
				"-t",
				"202108050500"
		};

		FloodPotentialSearchArguments arguments = FloodPotentialSearchArguments.instance();
		new FloodPotentialSearchAdapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Assertions.assertTrue( PathUtils.isExists( "src/test/resources/Output/Output.nc" ) );
	}

	@AfterAll
	public static void after(){
		PathUtils.deleteIfExists( "src/test/resources/Output/Output.nc" );
	}
}
