package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Unit test of Model pre-adapter for running NCHC Wu Sacramento model.
 * 
 * @author Brad Chen
 *
 */
public class WuPreAdapterTest {

	@Before
	public void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources/Sacramento",
				"-i",
				"Rainfall.xml",
				"-o",
				"Time.DAT"
				};
		
		PiIOArguments arguments = PiIOArguments.instance();
		new WuSacramentoPreAdapter().execute( args, arguments );
	}

	@Test
	public void test(){
		Assert.assertTrue( PathUtils.isExists( "src/test/resources/Sacramento/Input/Time.DAT" ) );
		Assert.assertTrue( PathUtils.list( "src/test/resources/Sacramento/Input" )
				.stream()
				.anyMatch( path -> PathUtils.equalsExtension( path, FileType.TXT ) ) );
	}
}
