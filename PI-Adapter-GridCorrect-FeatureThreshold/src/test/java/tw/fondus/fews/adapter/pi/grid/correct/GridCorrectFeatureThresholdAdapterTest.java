package tw.fondus.fews.adapter.pi.grid.correct;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.fews.adapter.pi.grid.correct.argument.RunArguments;

import java.io.IOException;
import java.math.BigDecimal;

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
				"Towns.geojson",
				"-p",
				"depth_below_surface_simulated"
		};

		RunArguments arguments = RunArguments.instance();
		new GridCorrectFeatureThresholdAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( "src/test/resources/Output/Output.nc" ) ){
			StandardGrid grid = NetCDFGridMapper.fromSliceTYXModel( reader, "depth_below_surface_simulated", 0 );
			BigDecimal max = grid.max();
			Assertions.assertTrue( NumberUtils.equals( max, Numbers.ZERO ) );
		}
	}

	@AfterAll
	public static void after(){
		PathUtils.deleteIfExists( PathUtils.path( "src/test/resources/Output/Output.nc" ) );
	}
}
