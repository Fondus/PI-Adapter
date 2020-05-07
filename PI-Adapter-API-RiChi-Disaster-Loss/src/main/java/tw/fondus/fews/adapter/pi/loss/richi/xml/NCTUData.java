package tw.fondus.fews.adapter.pi.loss.richi.xml;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;


/**
 * POJO disaster loss of API XML configuration.
 * 
 * @author Chao
 *
 */
@lombok.Data
@Root( name = "DATA" )
public class NCTUData {
	@ElementList( inline = true, entry = "NctuLoss" )
	private List<NCTULoss> lossList;
}
