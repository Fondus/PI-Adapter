package tw.fondus.fews.adapter.pi.workflow.state.argument;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

import java.util.List;

/**
 * Model adapter arguments for workflow state with FEWS.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class WorkflowStateArguments extends PiIOArguments {
	@Parameter( names = { "--state" }, required = true, description = "The state of workflow state." )
	private boolean state;

	@Parameter( names = { "--properties" },
				description = "The properties with comma, and format is key:value.",
				listConverter = PropertiesConverter.class )
	private List<PropertiesConverter.KeyValue> properties;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 */
	public static WorkflowStateArguments instance(){
		return WorkflowStateArguments.builder().build();
	}
}
