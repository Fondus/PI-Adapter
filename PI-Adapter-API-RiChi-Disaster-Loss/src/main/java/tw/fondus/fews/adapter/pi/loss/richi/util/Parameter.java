package tw.fondus.fews.adapter.pi.loss.richi.util;

/**
 * Parameter of Disaster Loss API.
 * 
 * @author Chao
 *
 */
public enum Parameter {
	C1LOSS ( "C1LOSS","10K TWD" ),
	C1AREA ( "C1AREA" , "m^2" ),
	C2LOSS ( "C2LOSS" , "10K TWD" ),
	C2AREA ( "C2AREA" , "m^2" ),
	C3LOSS ( "C3LOSS" , "10K TWD" ),
	C3AREA ( "C3AREA" , "m^2" ),
	C4LOSS ( "C4LOSS" , "10K TWD" ),
	C4AREA ( "C4AREA" , "m^2" ),
	H1LOSS ( "H1LOSS" , "10K TWD" ),
	H1COUNT( "H1COUNT" , "m^2" ),
	H2LOSS ( "H2LOSS" , "10K TWD" ),
	H2COUNT( "H2COUNT" , "m^2" ),
	F1LOSS ( "F1LOSS" , "10K TWD" ),
	F1AREA ( "F1AREA" , "m^2" ),
	F2LOSS ( "F2LOSS" , "10K TWD" ),
	F2AREA ( "F2AREA" , "m^2" ),
	F3LOSS ( "F3LOSS" , "10K TWD" ),
	F3AREA ( "F3AREA" , "m^2" ),
	F4LOSS ( "F4LOSS" , "10K TWD" ),
	F4AREA ( "F4AREA" , "m^2" ),
	F5LOSS ( "F5LOSS" , "10K TWD" ),
	F5AREA ( "F5AREA" , "m^2" ),
	F6LOSS ( "F6LOSS" , "10K TWD" ),
	F6AREA ( "F6AREA" , "m^2" ),
	PLOSS  ( "PLOSS" , "10K TWD" ),
	PAREA  ( "PAREA" , "m^2" );

	private String type;
	private String unit;

	private Parameter( String type, String unit ) {
		this.type = type;
		this.unit = unit;
	}
	
	public String getType(){
		return type;
	}

	public String getUnit() {
		return unit;
	}
}
