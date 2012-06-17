/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

/**
 * @author obergner
 * 
 */
public class PingResponse extends GsmPdu {

	private static final long	serialVersionUID	= 8968135952963388189L;

	public static PingResponse accept(final PingRequest pingRequest) {
		return new PingResponse(Acknowledgement.ack(), pingRequest);
	}

	public static PingResponse reject(final PingRequest pingRequest) {
		return new PingResponse(Acknowledgement.nack(), pingRequest);
	}

	private final Acknowledgement	ack;

	private final PingRequest	  pingRequest;

	private PingResponse(final Acknowledgement ack,
	        final PingRequest pingRequest) {
		super();
		notNull(ack, "Argument 'ack' must not be null");
		notNull(pingRequest, "Argument 'pingRequest' must not be null");
		this.ack = ack;
		this.pingRequest = pingRequest;
	}

	public Acknowledgement getAck() {
		return this.ack;
	}

	public boolean pingSucceeded() {
		return this.ack.is(Acknowledgement.Status.ACK);
	}

	public PingRequest getPingRequest() {
		return this.pingRequest;
	}

	@Override
	public String toString() {
		return "PingResponse@" + this.hashCode() + "[ack: " + this.ack
		        + "|pingRequest: " + this.pingRequest + "]";
	}
}
