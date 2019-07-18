package tw.fondus.fews.adapter.pi.runoff.nchc.msfrm;

import java.nio.file.Path;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffExecutable;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Model executable-adapter for running NCHC MSFRM model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class MSFRMExecutable extends RainRunoffExecutable {
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new MSFRMExecutable().execute( args, arguments );
	}

	@Override
	protected void addParameterFiles( Path parameterFile, String prefix ) {
		String key = PathUtils.getNameWithoutExtension( parameterFile ).split( prefix )[1];
		this.parametersMap.put( key, parameterFile.toString() );
	}
}
