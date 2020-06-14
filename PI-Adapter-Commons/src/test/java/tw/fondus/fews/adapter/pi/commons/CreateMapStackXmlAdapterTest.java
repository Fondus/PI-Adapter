package tw.fondus.fews.adapter.pi.commons;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.fews.adapter.pi.argument.extend.MapStackArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The unit test of CreateMapStackXmlAdapter.
 *
 * @author Brad Chen
 *
 */
public class CreateMapStackXmlAdapterTest {

	@Before
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"-i",
				"LocationId",
				"-o",
				"map.xml",
				"-p",
				"Depth",
				"-t",
				"201912021600",
				"-d",
				"2",
				"-td",
				"end",
				"-gt",
				"TWD 1997",
				"-n",
				"dm1d????.asc"
		};

		MapStackArguments arguments = MapStackArguments.instance();
		new CreateMapStackXmlAdapter().execute( args, arguments );
	}

	@Test
	public void test() throws Exception {
		MapStacks mapStacks = XMLUtils.fromXML( Paths.get( "src/test/resources/Output/map.xml" ), MapStacks.class );
		Assert.assertEquals( "TWD 1997", mapStacks.getGeoDatum() );
		Assert.assertEquals( 1, mapStacks.getMapStacks().size() );

		MapStack mapStack = mapStacks.getMapStacks().get( 0 );
		Assert.assertEquals( "LocationId", mapStack.getLocationId() );
		Assert.assertEquals( "Depth", mapStack.getParameterId() );
		Assert.assertEquals( "dm1d????.asc", mapStack.getFile().getPattern().getFile() );
		Assert.assertEquals( "hour", mapStack.getTimeStep().getUnit() );
		Assert.assertEquals( 1, mapStack.getTimeStep().getMultiplier() );
		Assert.assertEquals( "2019-12-02", mapStack.getStartDate().getDate() );
		Assert.assertEquals( "16:00:00", mapStack.getStartDate().getTime() );
		Assert.assertEquals( "2019-12-02", mapStack.getEndDate().getDate() );
		Assert.assertEquals( "18:00:00", mapStack.getEndDate().getTime() );
	}

	@After
	public void clear() throws IOException {
		Files.deleteIfExists( Paths.get( "src/test/resources/Output/map.xml" ) );
	}
}
