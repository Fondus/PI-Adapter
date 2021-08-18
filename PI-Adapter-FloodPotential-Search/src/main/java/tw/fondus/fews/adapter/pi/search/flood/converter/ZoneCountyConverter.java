package tw.fondus.fews.adapter.pi.search.flood.converter;

import com.beust.jcommander.IStringConverter;
import tw.fondus.commons.util.zone.ZoneCounty;

import java.util.stream.Stream;

/**
 * The converter of flood potential map supports zone.
 *
 * @author Brad Chen
 *
 */
public class ZoneCountyConverter implements IStringConverter<ZoneCounty> {
	@Override
	public ZoneCounty convert( String s ) {
		return Stream.of( ZoneCounty.values() )
				.filter( zone -> zone.value().equals( s ) )
				.findFirst()
				.orElse( null );
	}
}
