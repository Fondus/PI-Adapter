package tw.fondus.fews.adapter.pi.loss.richi.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root( name = "DATA" )
public class ErrorData {
	@Element( name = "Code" )
	private String code;
	
	@Element( name = "Message" )
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode( String code ) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage( String message ) {
		this.message = message;
	}
	
}
