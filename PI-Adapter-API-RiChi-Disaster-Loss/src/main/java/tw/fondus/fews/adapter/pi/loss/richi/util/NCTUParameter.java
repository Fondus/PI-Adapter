package tw.fondus.fews.adapter.pi.loss.richi.util;

/**
 * Parameter of Disaster Loss API.
 * 
 * @author Chao
 *
 */
public enum NCTUParameter {
	CLOSS ( "CLOSS" ,"10K TWD" ),
	HLOSS ( "HLOSS" , "10K TWD" ),
	FLOSS ( "FLOSS" , "10K TWD" ),
	PLOSS ( "PLOSS" , "10K TWD" ),
	LLOSS ( "LLOSS" , "10K TWD" ),
	ALOSS ( "ALOSS" , "10K TWD" );

	private String type;
	private String unit;

	private NCTUParameter( String type, String unit ) {
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
