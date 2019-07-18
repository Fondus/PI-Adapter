package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import java.nio.file.Path;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffExecutable;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Model executable-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoExecutable extends RainRunoffExecutable {

	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new WuSacramentoExecutable().execute( args, arguments );
	}

	@Override
	protected void addParameterFiles( Path parameterFile, String prefix ) {
		String key = PathUtils.getNameWithoutExtension( parameterFile ).split( prefix )[1];
		this.parametersMap.put( key, parameterFile.toString() );
	}
}
