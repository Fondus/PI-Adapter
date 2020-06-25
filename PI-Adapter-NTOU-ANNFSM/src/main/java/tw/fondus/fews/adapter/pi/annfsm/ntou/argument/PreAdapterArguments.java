package tw.fondus.fews.adapter.pi.annfsm.ntou.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.splitter.CommaSplitter;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NTOU ANNFSM model.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class PreAdapterArguments extends PiIOArguments {
	@Parameter( names = { "--coordinate",
			"-c" }, required = true, description = "The coordinate of CWB observation.", splitter = CommaSplitter.class )
	private List<String> coordinate;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static PreAdapterArguments instance() {
		return PreAdapterArguments.builder().build();
	}
}
