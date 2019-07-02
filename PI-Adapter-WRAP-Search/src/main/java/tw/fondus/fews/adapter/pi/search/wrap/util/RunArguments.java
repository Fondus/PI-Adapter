package tw.fondus.fews.adapter.pi.search.wrap.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

public class RunArguments extends PiArguments {

	@Parameter( names = { "--mdir",
			"-md" }, description = "The model merge directory path, relative to the current working directory." )
	private String mergeDir = "Merge/";

	@Parameter( names = { "--floodmapdir",
			"-fmd" }, description = "The flood map data directory path, relative to the current working directory." )
	private String floodMapDir = "FloodedMapData/Taiwan/";

	@Parameter( names = { "--region", "-r" }, required = true, description = "The region name." )
	private String region;
	@Parameter( names = { "--county", "-c" }, required = true, description = "The county name." )
	private String county;
	@Parameter( names = { "--duration", "-d" }, required = true, description = "The rainfall duration." )
	private String duration;

	public String getMergeDir() {
		return mergeDir;
	}

	public void setMergeDir( String mergeDir ) {
		this.mergeDir = mergeDir;
	}

	public String getFloodMapDir() {
		return floodMapDir;
	}

	public void setFloodMapDir( String floodMapDir ) {
		this.floodMapDir = floodMapDir;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion( String region ) {
		this.region = region;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty( String county ) {
		this.county = county;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration( String duration ) {
		this.duration = duration;
	}

}
