package tw.fondus.fews.adapter.pi.trigrs.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model post-adapter arguments for running TRIGRS landslide model.
 * 
 * @author Brad Chen
 *
 */
public class PostArguments extends PiArguments {
	@Parameter(names = { "--endperiod", "-e" }, required = true, description = "The Model metainfo end period of index.")
	private int after;

	public int getAfter() {
		return after;
	}

	public void setAfter(int after) {
		this.after = after;
	}
}
