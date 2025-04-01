package tw.fondus.fews.adapter.pi.report.rmo07.vo.report;

import lombok.Builder;
import lombok.Data;
import tw.fondus.commons.util.string.Strings;

import java.math.BigDecimal;

/**
 * The value object of waterlevel report record.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
public class WaterlevelRecord {
	private String branch;
	private String town;
	private String name;
	@Builder.Default
	private String warningNon = Strings.BLANK;
	@Builder.Default
	private String warningLevel1 = Strings.BLANK;
	@Builder.Default
	private String warningLevel2 = Strings.BLANK;
	@Builder.Default
	private String warningLevel3 = Strings.BLANK;
	@Builder.Default
	private String overDike = Strings.BLANK;
	private BigDecimal value;
	@Builder.Default
	private String time = Strings.BLANK;
}
