package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.util.ArrayList;
import java.util.List;

import tw.fondus.fews.adapter.pi.loss.richi.xml.NCTULoss;

/**
 * The entity of collection for disaster loss.
 * 
 * @author Chao
 *
 */
public class NCTULossCollection {
	private List<NCTULoss> lossList;
	private List<Long> dataTimeLongList;

	public NCTULossCollection(NCTULoss loss, long dataTimeLong) {
		lossList = new ArrayList<>();
		this.lossList.add( loss );
		dataTimeLongList = new ArrayList<>();
		this.dataTimeLongList.add( dataTimeLong );
	}

	public List<NCTULoss> getLossList() {
		return lossList;
	}

	public void setLossList( List<NCTULoss> lossList ) {
		this.lossList = lossList;
	}

	public List<Long> getDataTimeLongList() {
		return dataTimeLongList;
	}

	public void setDataTimeLongList( List<Long> dataTimeLongList ) {
		this.dataTimeLongList = dataTimeLongList;
	}

	public void addData( NCTULoss loss, long dataTimeLong ) {
		this.lossList.add( loss );
		this.dataTimeLongList.add( dataTimeLong );
	}

}
