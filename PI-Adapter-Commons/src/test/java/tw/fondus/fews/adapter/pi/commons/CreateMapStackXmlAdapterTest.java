package tw.fondus.fews.adapter.pi.commons;

import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.extend.MapStackArguments;

/**
 * The unit test of CreateMapStackXmlAdapter.
 *
 * @author Brad Chen
 *
 */
public class CreateMapStackXmlAdapterTest {

	@Test
	public void run(){
		String[] args = new String[]{
				"-b",
				"src/test/resources/",
				"-i",
				"LocationId",
				"-o",
				"map.xml",
				"-p",
				"Depth",
				"-st",
				"201912021600",
				"-d",
				"2",
				"-gt",
				"TWD 1997",
				"-n",
				"dm1d????.asc"
		};

		MapStackArguments arguments = new MapStackArguments();
		new CreateMapStackXmlAdapter().execute( args, arguments );
	}
}
