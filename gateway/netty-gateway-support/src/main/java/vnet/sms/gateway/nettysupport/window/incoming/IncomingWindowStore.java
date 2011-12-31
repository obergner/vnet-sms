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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public class IncomingWindowStore<ID extends Serializable> implements
        IncomingWindowStoreMBean {

	private final int	                     maximumCapacity;

	private final long	                     waitTimeMillis;

	private final ConcurrentMap<ID, Message>	messageReferenceToMessage;

	private final Semaphore	                 availableWindows;

	private volatile boolean	             shutDown	= false;

	public IncomingWindowStore(final int maximumCapacity,
	        final long waitTimeMillis) {
		this.maximumCapacity = maximumCapacity;
		this.availableWindows = new Semaphore(maximumCapacity);
		this.waitTimeMillis = waitTimeMillis;
		this.messageReferenceToMessage = new ConcurrentHashMap<ID, Message>(
		        maximumCapacity);
	}

	public final ObjectName objectNameFor(final Channel channel)
	        throws MalformedObjectNameException {
		return new ObjectName(
		        "vnet.sms:service=IncomingWindowStore,owner=channel-"
		                + channel.getId());
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getMaximumCapacity()
	 */
	@Override
	public final int getMaximumCapacity() {
		return this.maximumCapacity;
	}

	public long getWaitTimeMillis() {
		return this.waitTimeMillis;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getCurrentMessageCount()
	 */
	@Override
	public final int getCurrentMessageCount() {
		return this.messageReferenceToMessage.size();
	}

	public boolean tryAcquireWindow(
	        final WindowedMessageEvent<ID, ? extends Message> messageEvent)
	        throws IllegalArgumentException, InterruptedException {
		notNull(messageEvent, "Cannot store a null message");
		isTrue(messageEvent.getMessage() instanceof Message,
		        "Can only process MessageEvents containing a message of type "
		                + Message.class.getName() + ". Got: "
		                + messageEvent.getMessage());
		ensureNotShutDown();
		if (!this.availableWindows.tryAcquire(this.waitTimeMillis,
		        TimeUnit.MILLISECONDS)) {
			return false;
		}
		return storeMessageHavingPermit(messageEvent);
	}

	private void ensureNotShutDown() throws IllegalArgumentException {
		if (this.shutDown) {
			throw new IllegalArgumentException(
			        "This IncomingWindowStore has already been shut down");
		}
	}

	private boolean storeMessageHavingPermit(
	        final WindowedMessageEvent<ID, ? extends Message> messageEvent)
	        throws IllegalArgumentException {
		final Message storedMessageHavingSameId;
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

	public Message releaseWindow(final ID messageReference)
	        throws IllegalArgumentException {
		ensureNotShutDown();
		try {
			final Message releasedMessage = this.messageReferenceToMessage
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

	public Map<ID, Message> shutDown() {
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
