package tw.fondus.fews.adapter.pi.workflow.state.argument;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import tw.fondus.commons.cli.util.JCommanderRunner;

/**
 * The unit test of WorkflowStateArguments.
 *
 * @author Brad Chen
 *
 */
public class WorkflowStateArgumentsTest {
	@Test
	public void test(){
		String[] args = {
				"-b",
				"src/test/resources",
				"-i",
				"",
				"-o",
				"state.json",
				"--task-id",
				"test",
				"--state",
				"--properties",
				"key1:value1,key2:value2,key3:value3"
		};

		JCommanderRunner.execute( args, WorkflowStateArguments.instance(), "test", arguments -> {
			Assertions.assertTrue( arguments.isState() );
			Assertions.assertEquals( "test", arguments.getTaskRunId() );
			Assertions.assertEquals( 3, arguments.getProperties().size() );
			Assertions.assertEquals( "key2", arguments.getProperties().get( 1 ).getKey() );
			Assertions.assertEquals( "value2", arguments.getProperties().get( 1 ).getValue() );
		} );
	}
}
