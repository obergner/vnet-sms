/**
 * 
 */
package vnet.sms.common.messages;

import java.net.SocketAddress;

/**
 * @author obergner
 * 
 */
public class PingRequest extends Message {

	private static final long	serialVersionUID	= 6318807185475767936L;

	public PingRequest(final SocketAddress sender, final SocketAddress receiver) {
		super(sender, receiver);
	}

	@Override
	public String toString() {
		return "PingRequest@" + hashCode() + "[ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp() + "|sender: "
		        + getSender() + "|receiver: " + getReceiver() + "]";
	}
}
