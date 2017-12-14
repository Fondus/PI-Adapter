package tw.fondus.fews.adapter.pi.runoff.nchc.sacramento;

import java.io.File;

import nl.wldelft.util.FileUtils;
import tw.fondus.fews.adapter.pi.runoff.nchc.RainRunoffExecutable;
import tw.fondus.fews.adapter.pi.runoff.nchc.util.RunArguments;

/**
 * Model executable-adapter for running NCHC Wu Sacramento model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class WuSacramentoExecutable extends RainRunoffExecutable {

	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new WuSacramentoExecutable().execute(args, arguments);
	}

	@Override
	protected void addParameterFiles(File parameterFile, String prefix) {
		String key = FileUtils.getNameWithoutExt(parameterFile).split(prefix)[1];
		this.parametersMap.put(key, parameterFile.getPath());
	}
}
