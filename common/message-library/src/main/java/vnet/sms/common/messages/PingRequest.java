/**
 * 
 */
package vnet.sms.common.messages;

/**
 * @author obergner
 * 
 */
public class PingRequest extends Message {

	private static final long	serialVersionUID	= 6318807185475767936L;

	public PingRequest() {
		super();
	}

	@Override
	public String toString() {
		return "PingRequest@" + hashCode() + "[ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp() + "]";
	}
}
