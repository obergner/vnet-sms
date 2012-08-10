package vnet.sms.gateway.nettysupport.window.incoming;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public class IncomingWindowStore<ID extends Serializable> {

	private final int	                    maximumCapacity;

	private final long	                    waitTimeMillis;

	private final ConcurrentMap<ID, GsmPdu>	messageReferenceToMessage;

	private final Semaphore	                availableWindows;

	private volatile boolean	            shutDown	= false;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public IncomingWindowStore(final int maximumCapacity,
	        final long waitTimeMillis) {
		this.maximumCapacity = maximumCapacity;
		this.availableWindows = new Semaphore(maximumCapacity);
		this.waitTimeMillis = waitTimeMillis;
		this.messageReferenceToMessage = new ConcurrentHashMap<ID, GsmPdu>(
		        maximumCapacity);
	}

	// ------------------------------------------------------------------------
	// JMX API
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getMaximumCapacity()
	 */
	public final int getMaximumCapacity() {
		return this.maximumCapacity;
	}

	public long getWaitTimeMillis() {
		return this.waitTimeMillis;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getCurrentMessageCount()
	 */
	public final int getCurrentMessageCount() {
		return this.messageReferenceToMessage.size();
	}

	// ------------------------------------------------------------------------
	// Store new message
	// ------------------------------------------------------------------------

	/**
	 * @param channelEvent
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public boolean tryAcquireWindow(
	        final WindowedMessageEvent<ID, ? extends GsmPdu> messageEvent)
	        throws IllegalArgumentException, IllegalStateException,
	        InterruptedException {
		notNull(messageEvent, "Cannot store a null message");
		isTrue(messageEvent.getMessage() instanceof GsmPdu,
		        "Can only process MessageEvents containing a message of type "
		                + GsmPdu.class.getName() + ". Got: "
		                + messageEvent.getMessage());
		ensureNotShutDown();
		if (!this.availableWindows.tryAcquire(this.waitTimeMillis,
		        TimeUnit.MILLISECONDS)) {
			return false;
		}
		return storeMessageHavingPermit(messageEvent);
	}

	private void ensureNotShutDown() throws IllegalStateException {
		if (this.shutDown) {
			throw new IllegalStateException(
			        "This IncomingWindowStore has already been shut down");
		}
	}

	private boolean storeMessageHavingPermit(
	        final WindowedMessageEvent<ID, ? extends GsmPdu> messageEvent)
	        throws IllegalArgumentException {
		final GsmPdu storedMessageHavingSameId;
		if ((storedMessageHavingSameId = this.messageReferenceToMessage
		        .putIfAbsent(messageEvent.getMessageReference(),
		                messageEvent.getMessage())) != null) {
			throw new IllegalArgumentException("Another message ["
			        + storedMessageHavingSameId + "] having the same ID ["
			        + messageEvent.getMessageReference()
			        + "] has already been stored");
		}

		return true;
	}

	// ------------------------------------------------------------------------
	// Release message from store
	// ------------------------------------------------------------------------

	/**
	 * @param messageReference
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	public GsmPdu releaseWindow(final ID messageReference)
	        throws IllegalArgumentException, IllegalStateException {
		ensureNotShutDown();
		try {
			final GsmPdu releasedMessage = this.messageReferenceToMessage
			        .remove(messageReference);
			if (releasedMessage == null) {
				throw new IllegalArgumentException(
				        "Illegal attempt to release a non-existing message: no message having the supplied messageReference ["
				                + messageReference + "] is stored");
			}

			return releasedMessage;
		} finally {
			this.availableWindows.release();
		}
	}

	// ------------------------------------------------------------------------
	// Shutdown this store
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public Map<ID, GsmPdu> shutDown() {
		if (this.shutDown) {
			return Collections.emptyMap();
		}
		this.shutDown = true; // Volatile write

		return Collections.unmodifiableMap(this.messageReferenceToMessage);
	}

	@Override
	public String toString() {
		return "IncomingWindowStore@" + hashCode() + "[maximumCapacity = "
		        + this.maximumCapacity + "|messageReferenceToMessage = "
		        + this.messageReferenceToMessage + "|shutDown = "
		        + this.shutDown + "]";
	}
}
