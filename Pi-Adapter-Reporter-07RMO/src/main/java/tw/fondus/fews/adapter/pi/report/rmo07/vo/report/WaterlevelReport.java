package tw.fondus.fews.adapter.pi.report.rmo07.vo.report;

import lombok.Builder;
import lombok.Data;
import tw.fondus.commons.util.collection.CollectionUtils;

import java.util.List;

/**
 * The value object of waterlevel report.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
public class WaterlevelReport {
	public static final String[] NAMES = {
			"1-2時", "1-6時", "1-12時", "7-24時", "1-24時"
	};

	private String from;
	private String to;
	private String name;
	@Builder.Default
	private List<WaterlevelGroup> groups = CollectionUtils.emptyListArray();
}
