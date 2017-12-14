package tw.fondus.fews.adapter.pi.runoff.nchc.tank;

import java.io.File;

import nl.wldelft.util.FileUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffExecutable;
import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Model executable-adapter for running NCHC Tank model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TankExecutable extends RainRunoffExecutable {
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new TankExecutable().execute(args, arguments);
	}
	
	@Override
	protected void addParameterFiles(File parameterFile, String prefix) {
		String key = FileUtils.getNameWithoutExt(parameterFile);
		this.parametersMap.put(key, parameterFile.getPath());
	}
}
