package tw.fondus.fews.adapter.pi.runoff.nchc;

import lombok.Setter;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.junit.After;
import org.junit.Assert;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The unit test of NCHC RR post adapter test case.
 *
 * @author Brad Chen
 *
 */
public class PostAdapterTestCase {
	@Setter
	private String testCase;

	/**
	 * Get output test stream.
	 *
	 * @return output test stream
	 */
	protected Stream<Path> getOutputStream(){
		return PathUtils.list( "src/test/resources/" + this.testCase + "/Output" )
				.stream();
	}

	/**
	 * The test process.
	 *
	 * @throws IOException has IO Exception
	 */
	@SuppressWarnings( "rawtypes" )
	protected void testProcess() throws IOException {
		TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( PathUtils.path( "src/test/resources/" + this.testCase + "/Output/Output.xml" ) );
		Assert.assertFalse( timeSeriesArrays.isEmpty() );

		var list = this.getOutputStream()
				.filter( path -> PathUtils.equalsExtension( path, FileType.TXT ) )
				.map( PathUtils::getNameWithoutExtension )
				.collect( Collectors.toList());

		TimeSeriesLightUtils.forEach( timeSeriesArrays, array ->
				Assert.assertTrue( list.contains( array.getHeader().getLocationId() ) )
		);
	}

	@After
	public void clean(){
		this.getOutputStream()
				.filter( path -> PathUtils.equalsExtension( path, FileType.TXT ) )
				.forEach( PathUtils::deleteIfExists );
		PathUtils.deleteIfExists( "src/test/resources/" + this.testCase + "/Output/Output.xml" );
		PathUtils.deleteIfExists( "src/test/resources/" + this.testCase + "/Input/Time.DAT" );
	}
}
