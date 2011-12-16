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
public class PingResponse extends Message {

	private static final long	serialVersionUID	= 8968135952963388189L;

	public static PingResponse respondTo(final PingRequest pingRequest,
	        final SocketAddress sender, final SocketAddress receiver) {
		return new PingResponse(sender, receiver, pingRequest);
	}

	private final SocketAddress	sender;

	private final SocketAddress	receiver;

	private final PingRequest	pingRequest;

	private PingResponse(final SocketAddress sender,
	        final SocketAddress receiver, final PingRequest pingRequest) {
		notNull(sender, "Argument 'sender' must not be null");
		notNull(receiver, "Argument 'receiver' must not be null");
		notNull(pingRequest, "Argument 'pingRequest' must not be null");
		this.sender = sender;
		this.receiver = receiver;
		this.pingRequest = pingRequest;
	}

	public SocketAddress getSender() {
		return this.sender;
	}

	public SocketAddress getReceiver() {
		return this.receiver;
	}

	public PingRequest getPingRequest() {
		return this.pingRequest;
	}

	@Override
	public String toString() {
		return "PingResponse@" + hashCode() + " [ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp() + "|sender: "
		        + this.sender + "|receiver: " + this.receiver
		        + "|pingRequest: " + this.pingRequest + "]";
	}

}
