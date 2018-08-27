package tw.fondus.fews.adapter.pi.loss.richi.xml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;

import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;

public class DisasterLossTest {
	private Path path;

	@Before
	public void setUp() {
		path = Paths.get( "src/test/resources/DisasterLoss.xml" );
		Preconditions.checkState( Files.exists( path ), "Can not find disaster loss xml." );
	}

	@Test
	public void run() throws Exception {
		Data data = XMLUtils.fromXML( path.toFile(), Data.class );
		System.out.println( data.getLossList().get( 0 ).getTownId() );
		Assert.assertNotNull( data );
	}
}
