package tw.fondus.fews.adapter.pi.workflow.state;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tw.fondus.commons.json.util.JSONUtils;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.fews.adapter.pi.workflow.state.argument.WorkflowStateArguments;
import tw.fondus.fews.adapter.pi.workflow.state.vo.WorkflowState;

import java.nio.file.Path;

/**
 * The unit test of WorkflowStateAdapter.
 *
 * @author Brad Chen
 *
 */
public class WorkflowStateAdapterTest {
	@BeforeAll
	public static void run() {
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

		WorkflowStateArguments arguments = WorkflowStateArguments.instance();
		new WorkflowStateAdapter().execute( args, arguments );
	}

	@Test
	public void test() {
		Path stateJson = PathUtils.path( "src/test/resources/Input/state.json" );
		Assertions.assertTrue( PathUtils.isExists( stateJson ) );

		String jsonContent = PathReader.readString( stateJson );
		Assertions.assertTrue( JSONUtils.isJSON( jsonContent ) );

		WorkflowState state = GsonMapperRuntime.DEFAULT.toBean( jsonContent, WorkflowState.class );
		Assertions.assertTrue( state.isState() );
		Assertions.assertEquals( "test", state.getTaskId() );
		Assertions.assertTrue( state.getProperties().containsKey( "key2" ) );
		Assertions.assertEquals( "value2", state.getProperties().get( "key2" ) );
	}

	@AfterAll
	public static void finished() {
		PathUtils.deleteIfExists( "src/test/resources/Input/state.json" );
	}
}
