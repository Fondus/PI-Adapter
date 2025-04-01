package tw.fondus.fews.adapter.pi.report.rmo07.vo;

import lombok.Builder;
import lombok.Data;
import tw.fondus.commons.util.math.Numbers;

import java.math.BigDecimal;

/**
 * The instantaneous statistics result.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
public class InstantStatistics {
	private String id;
	@Builder.Default
	private BigDecimal rangeMax01To02 = Numbers.MISSING;
	@Builder.Default
	private BigDecimal rangeMax01To06 = Numbers.MISSING;
	@Builder.Default
	private BigDecimal rangeMax01To12 = Numbers.MISSING;
	@Builder.Default
	private BigDecimal rangeMax01To24 = Numbers.MISSING;
	@Builder.Default
	private BigDecimal rangeMax07To24 = Numbers.MISSING;

	// Time
	private String timeRangeMax01To02;
	private String timeRangeMax01To06;
	private String timeRangeMax01To12;
	private String timeRangeMax01To24;
	private String timeRangeMax07To24;
}
