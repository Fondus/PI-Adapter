package tw.fondus.fews.adapter.pi.aws.storage.converter;

import com.beust.jcommander.IStringConverter;
import tw.fondus.fews.adapter.pi.aws.storage.util.PredefinePrefix;

import java.util.stream.Stream;

/**
 * The converter of predefine prefix type.
 *
 * @author Brad Chen
 *
 */
public class PredefinePrefixConverter implements IStringConverter<PredefinePrefix> {
	@Override
	public PredefinePrefix convert( String s ) {
		return Stream.of( PredefinePrefix.values() )
				.filter( prefix -> prefix.name().equals( s ) )
				.findFirst()
				.orElse( PredefinePrefix.USER );
	}
}
