package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import strman.Strman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The unit test of ModelUtils.
 *
 * @author Brad Chen
 *
 */
public class ModelUtilsTest {
	private Path areaOrderFilePath;
	private Path parameterFilePath;

	@Before
	public void checkExist(){
		String root = "src/test/resources/Template/";
		String region = "Hsinchu_ZhuDong/";
		String caseName = "Case1/";

		String regionPathText = Strman.append( root, region );
		String casePathText = Strman.append( regionPathText, caseName );

		this.areaOrderFilePath = Paths.get( Strman.append( regionPathText, ModelFileNames.AREA_ORDER ) );
		this.parameterFilePath = Paths.get( Strman.append( casePathText, ModelFileNames.PARAMETER ) );

		Assert.assertTrue( Files.exists( this.areaOrderFilePath ) );
		Assert.assertTrue( Files.exists( this.parameterFilePath ) );
	}

	@Test
	public void testDuration() throws IOException {
		Assert.assertEquals( ModelUtils.readModelDuration( this.parameterFilePath.toString() ), 24 );
	}

	@Test
	public void testAreaOrder() throws IOException {
		List<String> areas = ModelUtils.readAreaOrder( this.areaOrderFilePath.toString() );
		Assert.assertTrue( areas.size() > 0 );

		areas.forEach( area -> System.out.println( area ) );
	}
}
