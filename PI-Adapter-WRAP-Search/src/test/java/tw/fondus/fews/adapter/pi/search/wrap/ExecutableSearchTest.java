package tw.fondus.fews.adapter.pi.search.wrap;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.search.wrap.argument.RunArguments;
/**
 * The Unit Test for WRAP Search.
 * 
 * @author shepherd
 *
 */
public class ExecutableSearchTest {

	@Test
	public void test() {

		String[] args = new String[] {
				"-b",
				"src/test/reousrces",
				"-i",
				"Rainfall.xml,Level.txt",
				"-o",
				"",
				"-r",
				"Southern",
				"-c",
				"Chiayi",
				"-d",
				"24h"
		};
		RunArguments arguments = RunArguments.instance();
		new SearchExecutable().execute( args, arguments );
	}
}
