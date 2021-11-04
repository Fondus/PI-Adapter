package tw.fondus.fews.adapter.pi.workflow.state.argument;

import com.beust.jcommander.IStringConverter;
import lombok.Builder;
import lombok.Data;
import tw.fondus.commons.util.string.Strings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertiesConverter implements IStringConverter<List<PropertiesConverter.KeyValue>> {
	@Override
	public List<KeyValue> convert( String s ) {
		return Stream.of( s.split( "," ) )
				.map( string -> string.split( Strings.COLON ) )
				.map( splits -> KeyValue.builder()
						.key( splits[0] )
						.value( splits[1] )
						.build() )
				.collect( Collectors.toList() );
	}

	@Data
	@Builder
	public static class KeyValue {
		private String key;
		private String value;
	}
}
