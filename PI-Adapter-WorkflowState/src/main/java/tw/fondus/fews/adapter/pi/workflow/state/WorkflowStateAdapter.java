package tw.fondus.fews.adapter.pi.workflow.state;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.workflow.state.argument.PropertiesConverter;
import tw.fondus.fews.adapter.pi.workflow.state.argument.WorkflowStateArguments;
import tw.fondus.fews.adapter.pi.workflow.state.vo.WorkflowState;

import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * FEWS adapter used to create workflow state with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class WorkflowStateAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		WorkflowStateArguments arguments = WorkflowStateArguments.instance();
		new WorkflowStateAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath,
			Path inputPath, Path outputPath ) {
		// Cast PiArguments to expand arguments
		WorkflowStateArguments modelArguments = this.asArguments( arguments, WorkflowStateArguments.class );

		String taskId = modelArguments.getTaskRunId();
		logger.log( LogLevel.INFO, "WorkflowStateAdapter: Start to create workflow with task id: {}, state: {}.", taskId, modelArguments.isState() );
		WorkflowState.WorkflowStateBuilder builder = WorkflowState.builder()
				.taskRunId( taskId )
				.timeZero( JodaTimeUtils.toStringISO8601( modelArguments.getTimeZero() ) )
				.state( modelArguments.isState() );

		if ( modelArguments.getProperties().size() > 0 ){
			logger.log( LogLevel.INFO, "WorkflowStateAdapter: Mapping workflow related properties with task id: {}.", taskId );
			builder.properties( modelArguments.getProperties().stream()
					.collect( Collectors.toMap( PropertiesConverter.KeyValue::getKey, PropertiesConverter.KeyValue::getValue ) ) );
		}

		WorkflowState state = builder.build();
		Path output;
		if ( modelArguments.isWriteInput() ){
			logger.log( LogLevel.INFO, "WorkflowStateAdapter: Write workflow state with task id: {} into folder: {}.", taskId, inputPath );
			output = inputPath.resolve( modelArguments.getOutputs().get( 0 ) );
		} else {
			logger.log( LogLevel.INFO, "WorkflowStateAdapter: Write workflow state with task id: {} into folder: {}.", taskId, outputPath );
			output = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
		}
		PathWriter.write( output, GsonMapperRuntime.DEFAULT.toString( state ) );
		logger.log( LogLevel.INFO, "WorkflowStateAdapter: Finished to create workflow with task id: {}, state: {}.", taskId, modelArguments.isState() );
	}
}
