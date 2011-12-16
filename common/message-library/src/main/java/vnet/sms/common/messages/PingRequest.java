/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;

/**
 * @author obergner
 * 
 */
public class PingRequest extends Message {

	private static final long	serialVersionUID	= 6318807185475767936L;

	private final SocketAddress	sender;

	private final SocketAddress	receiver;

	public PingRequest(final SocketAddress sender, final SocketAddress receiver) {
		notNull(sender, "Argument 'sender' must not be null");
		notNull(receiver, "Argument 'receiver' must not be null");
		this.sender = sender;
		this.receiver = receiver;
	}

	public SocketAddress getSender() {
		return this.sender;
	}

	public SocketAddress getReceiver() {
		return this.receiver;
	}

	@Override
	public String toString() {
		return "PingRequest@" + hashCode() + " [ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp() + "|sender: "
		        + this.sender + "|receiver: " + this.receiver + "]";
	}
}
