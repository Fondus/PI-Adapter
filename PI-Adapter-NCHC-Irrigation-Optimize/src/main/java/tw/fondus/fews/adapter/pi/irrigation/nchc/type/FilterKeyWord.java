package tw.fondus.fews.adapter.pi.irrigation.nchc.type;

import lombok.Getter;

/**
 * The enum type use to filter.
 *
 * @author Brad Chen
 *
 */
public enum FilterKeyWord {
	DRAFT( "取水量" ),
	RATIO_WATER_REQUIREMENT( "需水量比例" ),
	PLAN_WATER_REQUIREMENT( "計畫需水量" ),
	AREA( "農作面積" ),
	AREA_TOTAL( "總農作面積" ),
	USER_DEFINE("供水方案" );

	@Getter
	private String value;

	FilterKeyWord( String value ){
		this.value = value;
	}
}
