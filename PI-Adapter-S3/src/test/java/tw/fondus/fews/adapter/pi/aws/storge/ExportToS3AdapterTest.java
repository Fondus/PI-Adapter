package tw.fondus.fews.adapter.pi.aws.storge;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.fews.adapter.pi.aws.storge.argument.S3Arguments;

/**
 * The integration test of ExportToS3Adapter.
 *
 * @author Brad Chen
 *
 */
public class ExportToS3AdapterTest {
	@BeforeAll
	public static void run() {
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"--host",
				"",
				"--bucket",
				"",
				"--object",
				"",
				"-i",
				"",
				"-o",
				"",
				"-us",
				"",
				"-pw",
				""
		};

		S3Arguments arguments = S3Arguments.instance();
		new ExportToS3Adapter().execute( args, arguments );
	}

	@Test
	public void test() {
		// to do
	}

	@AfterAll
	public static void after(){
		// to do
	}
}
