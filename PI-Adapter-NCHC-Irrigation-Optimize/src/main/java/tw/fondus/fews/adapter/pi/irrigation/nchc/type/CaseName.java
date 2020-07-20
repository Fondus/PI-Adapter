package tw.fondus.fews.adapter.pi.irrigation.nchc.type;

import lombok.Getter;

/**
 * The enum type of case.
 *
 * @author Brad Chen
 *
 */
public enum CaseName {
	CASE_1( "Case1", "即期現況配水" ),
	CASE_2( "Case2", "中短期現況配水" ),
	CASE_3( "Case3", "夜間減供配水" ),
	CASE_4( "Case4", "輪灌減供配水" ),
	CASE_5( "Case5", "強化輪灌與延長配水" ),
	CASE_6( "Case6", "使用者自定義" );

	@Getter
	private String name;

	@Getter
	private String description;

	CaseName( String name, String description ){
		this.name = name;
		this.description = description;
	}
}
