package tw.fondus.fews.adapter.pi.search.flood.argument;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.cli.util.JCommanderRunner;

/**
 * The unit test of FloodPotentialSearchArguments.
 *
 * @author Brad Chen
 *
 */
public class FloodPotentialSearchArgumentsTest {
	@Test
	public void test() {
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
				"12h"
		};

		FloodPotentialSearchArguments arguments = FloodPotentialSearchArguments.instance();
		JCommanderRunner.execute( args, arguments, this.getClass().getSimpleName(), runArguments -> {
			Assertions.assertEquals( "12h", runArguments.getAccumulatedRainfallDuration().getName() );
			Assertions.assertEquals( "Southern", runArguments.getArea().value() );
			Assertions.assertEquals( "Pingtung", runArguments.getZone().value() );
		} );
	}
}
