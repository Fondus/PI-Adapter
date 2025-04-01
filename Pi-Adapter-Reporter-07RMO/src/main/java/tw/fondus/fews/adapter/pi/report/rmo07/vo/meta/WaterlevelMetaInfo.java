package tw.fondus.fews.adapter.pi.report.rmo07.vo.meta;

import java.math.BigDecimal;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.util.math.NumberUtils;

/**
 * The meta-info value object of waterlevel station.
 *
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
public class WaterlevelMetaInfo {
	private String id;
	private String name;
	private BigDecimal warningLevel1;
	private BigDecimal warningLevel2;
	private BigDecimal warningLevel3;
	private BigDecimal topLevel;
	private String branch;
	private String town;
	private String crossSectionId;
	
	public boolean isMissingWarningLevel1() {
		return NumberUtils.isMissing( this.warningLevel1 );
	}

	public boolean isNotMissingWarningLevel1() {
		return NumberUtils.isNotMissing( this.warningLevel1 );
	}
	
	public boolean isMissingWarningLevel2() {
		return NumberUtils.isMissing( this.warningLevel2 );
	}

	public boolean isNotMissingWarningLevel2() {
		return NumberUtils.isNotMissing( this.warningLevel2 );
	}
	
	public boolean isMissingWarningLevel3() {
		return NumberUtils.isMissing( this.warningLevel3 );
	}

	public boolean isNotMissingWarningLevel3() {
		return NumberUtils.isNotMissing( this.warningLevel3 );
	}
	
	public boolean isMissingTopLevel() {
		return NumberUtils.isMissing( this.topLevel );
	}

	public boolean isNotMissingTopLevel() {
		return NumberUtils.isNotMissing( this.topLevel );
	}
}
