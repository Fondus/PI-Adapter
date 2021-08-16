package tw.fondus.fews.adapter.pi.search.flood.converter;

import com.beust.jcommander.IStringConverter;
import tw.fondus.fews.adapter.pi.search.flood.util.AccumulatedRainfallDuration;

import java.util.stream.Stream;

/**
 * The converter of supports duration of accumulated rainfall.
 *
 * @author Brad Chen
 *
 */
public class AccumulatedRainfallDurationConverter implements IStringConverter<AccumulatedRainfallDuration> {
	@Override
	public AccumulatedRainfallDuration convert( String s ) {
		return Stream.of( AccumulatedRainfallDuration.values() )
				.filter( duration -> duration.getName().equals( s ) )
				.findFirst()
				.orElse( AccumulatedRainfallDuration.D06H );
	}
}
