package tw.fondus.fews.adapter.pi.loss.richi.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import lombok.Data;

/**
 * 
 * 
 * @author Chao
 *
 */
@Data
@Root( name = "DATA" )
public class ErrorData {
	@Element( name = "Code" )
	private String code;
	
	@Element( name = "Message" )
	private String message;
}
