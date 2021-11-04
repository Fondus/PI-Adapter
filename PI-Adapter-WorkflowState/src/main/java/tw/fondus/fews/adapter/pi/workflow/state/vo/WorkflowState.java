package tw.fondus.fews.adapter.pi.workflow.state.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.fondus.commons.util.bean.IObjectProperties;
import tw.fondus.commons.util.collection.CollectionUtils;

import java.util.Map;

/**
 * The json model of workflow state.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowState implements IObjectProperties<String> {
	@Builder.Default
	private Map<String, Object> properties = CollectionUtils.emptyMapHash();
	@Builder.Default
	private boolean state = false;
	private String taskId;
	private String timeZero;
}
