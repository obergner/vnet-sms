/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

import java.util.UUID;

/**
 * @author obergner
 * 
 */
public abstract class Message extends GsmPdu {

	private static final long	serialVersionUID	= 4113467455546122911L;

	protected final Msisdn	  originator;

	protected final Msisdn	  destination;

	/**
	 * 
	 */
	protected Message(final Msisdn originator, final Msisdn destination) {
		this(UUID.randomUUID(), System.currentTimeMillis(), originator,
		        destination);
	}

	/**
	 * @param id
	 * @param creationTimestamp
	 */
	protected Message(final UUID id, final long creationTimestamp,
	        final Msisdn originator, final Msisdn destination) {
		super(id, creationTimestamp);
		notNull(originator, "Argument 'originator' must not be null");
		notNull(destination, "Argument 'destination' must not be null");
		this.originator = originator;
		this.destination = destination;
	}

	/**
	 * @return the originator
	 */
	public final Msisdn getOriginator() {
		return this.originator;
	}

	/**
	 * @return the destination
	 */
	public final Msisdn getDestination() {
		return this.destination;
	}
}
