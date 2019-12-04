package tw.fondus.fews.adapter.pi.annfsm.ntou.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.commons.cli.argument.converter.FileListConverter;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

/**
 * Model pre-adapter arguments for running NTOU ANNFSM model.
 * 
 * @author Chao
 *
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class PreAdapterArguments extends PiIOArguments {
	@Parameter( names = { "--coordinate",
			"-c" }, required = true, description = "The coordinate of CWB observation.", listConverter = FileListConverter.class )
	private List<String> coordinate;
}
