package tw.fondus.fews.adapter.pi.commons;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.converter.DateTimeConverter;
import tw.fondus.fews.adapter.pi.argument.extend.AdjustTimeArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The unit test of AdjustTimeContentAdapter.
 *
 * @author Brad Chen
 *
 */
public class AdjustTimeContentAdapterTest {
	@SuppressWarnings( "rawtypes" )
	@Test
	public void testMode0() throws IOException {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-od",
				"Input/",
				"-i",
				"Input.xml,Base.xml",
				"-o",
				"Output.xml",
				"-m",
				"0"
		};
		this.run( args );

		TimeSeriesArrays outputTimeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Input/Output.xml" ) );
		TimeSeriesArrays baseTimeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Input/Base.xml" ) );

		Assert.assertEquals( baseTimeSeriesArrays.get( 0 ).getTime( 0 ), outputTimeSeriesArrays.get( 0 ).getTime( 0 ) );
		Assert.assertEquals( baseTimeSeriesArrays.get( 0 ).getTime( 1 ), outputTimeSeriesArrays.get( 0 ).getTime( 1 ) );
		Assert.assertEquals( baseTimeSeriesArrays.get( 0 ).getTime( 2 ), outputTimeSeriesArrays.get( 0 ).getTime( 2 ) );
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void testMode1() throws IOException {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-od",
				"Input/",
				"-i",
				"Input.xml,Base.xml",
				"-o",
				"Output.xml",
				"-m",
				"1",
				"-t",
				"201902061800"
		};
		this.run( args );

		TimeSeriesArrays outputTimeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Input/Output.xml" ) );
		long timeStep = outputTimeSeriesArrays.getCommonTimeStepMillis();

		DateTime startTime = new DateTimeConverter().convert( "201902061800" );
		Assert.assertEquals( startTime.getMillis(), outputTimeSeriesArrays.get( 0 ).getTime( 0 ) );
		Assert.assertEquals( startTime.getMillis() + timeStep, outputTimeSeriesArrays.get( 0 ).getTime( 1 ) );
		Assert.assertEquals( startTime.getMillis() + timeStep * 2, outputTimeSeriesArrays.get( 0 ).getTime( 2 ) );
	}

	@After
	public void clear() throws IOException {
		Files.deleteIfExists( Paths.get( "src/test/resources/Input/Output.xml" ) );
	}

	/**
	 * Run the adapter by arguments.
	 *
	 * @param args adapter arguments
	 */
	private void run( String[] args ){
		AdjustTimeArguments arguments = AdjustTimeArguments.instance();
		new AdjustTimeContentAdapter().execute( args, arguments );
	}
}
