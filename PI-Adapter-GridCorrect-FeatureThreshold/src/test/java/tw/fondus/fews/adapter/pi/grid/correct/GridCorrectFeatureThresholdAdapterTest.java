package tw.fondus.fews.adapter.pi.grid.correct;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.fews.adapter.pi.grid.correct.argument.RunArguments;

public class GridCorrectFeatureThresholdAdapterTest {
	@BeforeAll
	public static void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"Input.xml,Input.nc",
				"-o",
				"Output.nc",
				"-d",
				"12",
				"-ts",
				"40",
				"-ff",
				"Towns.geojson"
		};

		RunArguments arguments = RunArguments.instance();
		new GridCorrectFeatureThresholdAdapter().execute( args, arguments );
	}

	@Test
	public void test() {

	}

	@AfterAll
	public static void after(){

	}
}
