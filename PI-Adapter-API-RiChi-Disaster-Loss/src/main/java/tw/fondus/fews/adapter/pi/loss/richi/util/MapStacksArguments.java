package tw.fondus.fews.adapter.pi.loss.richi.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;

public class MapStacksArguments extends PiBasicArguments{
	@Parameter( names = { "--mapstacks", "-m" }, required = true, description = "The pi MapStacks xml file name." )
	protected String mapStacks;

	public String getMapStacks() {
		return mapStacks;
	}

	public void setMapStacks( String mapStacks ) {
		this.mapStacks = mapStacks;
	}
	
}
