package tw.fondus.fews.adapter.pi.loss.richi.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;

@Data
@EqualsAndHashCode( callSuper = false )
public class ProcessArguments extends PiIOArguments{
	@Parameter( names = { "--version", "-v" }, description = "The version of disaster loss API.(1 for old version, 2 for new)" )
	private int version = 1;
}
