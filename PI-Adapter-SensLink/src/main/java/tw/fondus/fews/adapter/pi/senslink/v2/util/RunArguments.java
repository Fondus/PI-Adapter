package tw.fondus.fews.adapter.pi.senslink.v2.util;

import com.beust.jcommander.Parameter;

import tw.fondus.commons.fews.pi.util.adapter.PiArguments;

/**
 * Model adapter arguments for data exchange with SensLink 2.0.
 * 
 * @author Brad Chen
 *
 */
public class RunArguments extends PiArguments {
	@Parameter(names = { "--timeindex", "-ti" }, required = true, description = "The time index of the TimeSeriesArray index position")
	private int index;
	
	@Parameter(names = { "--duration", "-d" }, required = true, description = "The minus duration relative to the current time.")
	private int duration;
	
	@Parameter(names = { "--username", "-us" }, required = true, description = "The account username.")
	private String username;
	
	@Parameter(names = { "--password", "-pw" }, required = true, description = "The account password.")
	private String password;
	
	@Parameter(names = { "--server", "-s" }, required = true, description = "The target server, value can be 0 or 1 only.")
	private int server;

	public int getIndex() {
		return index;
	}

	public void setIndex( int index ) {
		this.index = index;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration( int duration ) {
		this.duration = duration;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public int getServer() {
		return server;
	}

	public void setServer( int server ) {
		this.server = server;
	}
}