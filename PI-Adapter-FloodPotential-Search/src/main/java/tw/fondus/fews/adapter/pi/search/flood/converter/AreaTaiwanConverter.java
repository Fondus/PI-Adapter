package tw.fondus.fews.adapter.pi.search.flood.converter;

import com.beust.jcommander.IStringConverter;
import tw.fondus.commons.util.zone.AreaTaiwan;

import java.util.stream.Stream;

/**
 * The converter of flood potential map supports area.
 *
 * @author Brad Chen
 *
 */
public class AreaTaiwanConverter implements IStringConverter<AreaTaiwan> {
	@Override
	public AreaTaiwan convert( String s ) {
		return Stream.of( AreaTaiwan.values() )
				.filter( area -> area.value().equals( s ) )
				.findFirst()
				.orElse( null );
	}
}
