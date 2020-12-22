package tw.fondus.fews.adapter.pi.ntu.qperif;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * The unit test of RIFModelPreAdapter.
 *
 * @author Brad Chen
 *
 */
public class RIFModelPreAdapterTest {
	@Before
	public void run(){
		String[] args = {
				"-b",
				"src/test/resources",
				"-id",
				"InputData/",
				"-od",
				"InputData/",
				"-i",
				"Tide.xml,Sensor.xml,InputOrder.csv",
				"-o",
				"tidal_level.txt,ObsFlo.csv"
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new RIFModelPreAdapter().execute( args, arguments );
	}

	@Test
	public void test(){
		var tide = PathReader.readAllLines( "src/test/resources/InputData/tidal_level.txt" ).get( 0 );
		Assert.assertEquals( -0.019, NumberUtils.create( tide ).doubleValue(), 0 );

		var sensors = PathReader.readAllLines( "src/test/resources/InputData/ObsFlo.csv" );
		var line1 = sensors.get( 1 ).split( Strings.COMMA );
		Assert.assertEquals( "2020093023", line1[0] );
		Assert.assertEquals( "-3", line1[1] );
		Assert.assertEquals( "0.0", line1[2] );
	}

	@After
	public void clean(){
		PathUtils.deleteIfExists( "src/test/resources/InputData/ObsFlo.csv" );
		PathUtils.deleteIfExists( "src/test/resources/InputData/tidal_level.txt" );
	}
}
