package tw.fondus.fews.adapter.pi.search.flood.util;

import lombok.Getter;
import tw.fondus.commons.util.string.Strings;

/**
 * The enum of supports duration of accumulated rainfall.
 *
 * @author Brad Chen
 *
 */
public enum AccumulatedRainfallDuration {
	D06H( "06h" ),
	D12H( "12h" ),
	D24H( "24h" );

	@Getter
	private final String name;

	AccumulatedRainfallDuration( String name ){
		this.name = name;
	}

	public String getFileNameRule(){
		return Strings.UNDERLINE +  this.name;
	}
}
