package tw.fondus.fews.adapter.pi.report.rmo07.vo.report;

import lombok.Builder;
import lombok.Data;
import tw.fondus.commons.util.collection.CollectionUtils;

import java.util.List;

/**
 * The value object of waterlevel report group.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
public class WaterlevelGroup {
	@Builder.Default
	private List<WaterlevelRecord> records = CollectionUtils.emptyListArray();
}
