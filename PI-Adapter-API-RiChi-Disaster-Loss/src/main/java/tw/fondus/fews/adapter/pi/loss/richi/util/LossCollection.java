package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.util.ArrayList;
import java.util.List;

import tw.fondus.fews.adapter.pi.loss.richi.xml.Loss;

/**
 * The entity of collection for disaster loss.
 * 
 * @author Chao
 *
 */
public class LossCollection {
	private List<Loss> lossList;
	private List<Long> dataTimeLongList;
	
	public LossCollection( Loss loss, long dataTimeLong){
		lossList = new ArrayList<>();
		this.lossList.add( loss );
		dataTimeLongList = new ArrayList<>();
		this.dataTimeLongList.add( dataTimeLong );
	}

	public List<Loss> getLossList() {
		return lossList;
	}

	public void setLossList( List<Loss> lossList ) {
		this.lossList = lossList;
	}
	
	public List<Long> getDataTimeLongList() {
		return dataTimeLongList;
	}

	public void setDataTimeLongList( List<Long> dataTimeLongList ) {
		this.dataTimeLongList = dataTimeLongList;
	}
	
	public void addData( Loss loss, long dataTimeLong ){
		this.lossList.add( loss );
		this.dataTimeLongList.add( dataTimeLong );
	}
	
}
