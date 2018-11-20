package tw.fondus.fews.adapter.pi.senslink.v2.util;

import tw.fondus.commons.json.senslink.v2.util.SensLinkUtils;

/**
 * The tools of PiAdaper-SensLink 2.0.
 * 
 * @author Brad Chen
 *
 */
public class AdapterUtils {
	
	/**
	 * Get the server from the SensLink 2.0.
	 * 
	 * @param server
	 * @return
	 */
	public static String getHost( int server ){
		switch ( server ) {
		case 0:
			return SensLinkUtils.HOST;
		case 1:
			return SensLinkUtils.HOST_TEST;
		default:
			return SensLinkUtils.HOST;
		}
	}
}
