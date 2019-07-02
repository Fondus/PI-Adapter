package tw.fondus.fews.adapter.pi.search.wrap;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.search.wrap.util.RunArguments;
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
				"localhost:\\Search_Flooded_Graph",
				"-i",
				"",
				"-o",
				"",
				"-r",
				"Southern",
				"-c",
				"Chiayi",
				"-d",
				"24h"
		};
		RunArguments arguments =new RunArguments();
		new SearchExecutable().execute( args, arguments );
	}
}
