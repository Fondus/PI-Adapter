package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import java.nio.file.Path;

import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffExecutable;
import tw.fondus.fews.adapter.pi.runoff.nchc.argument.RunArguments;

/**
 * Model executable-adapter for running NCHC Tank model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TankExecutable extends RainRunoffExecutable {
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new TankExecutable().execute( args, arguments );
	}

	@Override
	protected void addParameterFiles( Path parameterFile, String prefix ) {
		String key = PathUtils.getNameWithoutExtension( parameterFile );
		this.parametersMap.put( key, parameterFile.toString() );
	}
}
