package tw.fondus.fews.adapter.pi.commons;

import org.junit.jupiter.api.Test;
import tw.fondus.fews.adapter.pi.argument.extend.SleepArguments;

/**
 * The unit test of SleepAdapter.
 *
 * @author Brad Chen
 *
 */
public class SleepAdapterTest {
	@Test
	public void test(){
		String[] args = new String[]{
				"-b",
				"src/test/resources",
				"--sleep",
				"5000"
		};

		SleepArguments arguments = SleepArguments.instance();
		new SleepAdapter().execute( args, arguments );
	}
}
