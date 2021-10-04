package tw.fondus.fews.adapter.pi.commons;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The unit test of ilterMissingLocationXmlAdapter.
 *
 * @author Brad Chen
 *
 */
public class FilterMissingLocationXmlAdapterTest {
	@Before
	public void adapterRun() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-od",
				"Input/",
				"-i",
				"Filter.xml",
				"-o",
				"Output.xml"
		};

		PiIOArguments arguments = PiIOArguments.instance();
		new FilterMissingLocationXmlAdapter().execute( args, arguments );
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void test() throws IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( Paths.get( "src/test/resources/Input/Output.xml" ) );
		boolean containMissing = Stream.of( timeSeriesArrays.toArray() )
				.anyMatch( array -> {
					int size = array.size();
					return IntStream.range( 0, size ).anyMatch( array::isMissingValue );
				} );
		Assertions.assertFalse( containMissing );
	}

	@After
	public void clear() throws IOException {
		Files.deleteIfExists( Paths.get( "src/test/resources/Input/Output.xml" ) );
	}
}
