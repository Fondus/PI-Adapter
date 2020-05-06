package tw.fondus.fews.adapter.pi.loss.richi.xml;

import java.math.BigDecimal;

import org.simpleframework.xml.Element;

/**
 * POJO disaster loss of API XML configuration.
 * 
 * @author Chao
 *
 */
public class NCTULoss {
	@Element( name = "TOWN_ID" )
	private String townId;

	@Element( name = "TOWN_NA" )
	private String townName;
	
	@Element( name = "CLOSS" )
	private BigDecimal cLoss;
	
	@Element( name = "HLOSS" )
	private BigDecimal hLoss;
	
	@Element( name = "FLOSS" )
	private BigDecimal fLoss;
	
	@Element( name = "PLOSS" )
	private BigDecimal pLoss;
	
	@Element( name = "LLOSS" )
	private BigDecimal lLoss;
	
	@Element( name = "ALOSS" )
	private BigDecimal aLoss;

	public String getTownId() {
		return townId;
	}

	public void setTownId( String townId ) {
		this.townId = townId;
	}

	public String getTownName() {
		return townName;
	}

	public void setTownName( String townName ) {
		this.townName = townName;
	}

	public BigDecimal getcLoss() {
		return cLoss;
	}

	public void setcLoss( BigDecimal cLoss ) {
		this.cLoss = cLoss;
	}

	public BigDecimal gethLoss() {
		return hLoss;
	}

	public void sethLoss( BigDecimal hLoss ) {
		this.hLoss = hLoss;
	}

	public BigDecimal getfLoss() {
		return fLoss;
	}

	public void setfLoss( BigDecimal fLoss ) {
		this.fLoss = fLoss;
	}

	public BigDecimal getpLoss() {
		return pLoss;
	}

	public void setpLoss( BigDecimal pLoss ) {
		this.pLoss = pLoss;
	}

	public BigDecimal getlLoss() {
		return lLoss;
	}

	public void setlLoss( BigDecimal lLoss ) {
		this.lLoss = lLoss;
	}

	public BigDecimal getaLoss() {
		return aLoss;
	}

	public void setaLoss( BigDecimal aLoss ) {
		this.aLoss = aLoss;
	}

}
