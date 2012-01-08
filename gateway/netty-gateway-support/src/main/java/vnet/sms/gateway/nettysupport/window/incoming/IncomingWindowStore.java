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
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
@ManagedResource(description = "Buffers incoming messages until a predefined limit is reached.")
public class IncomingWindowStore<ID extends Serializable> {

	private final Logger	                 log	  = LoggerFactory
	                                                          .getLogger(getClass());

	private final int	                     maximumCapacity;

	private final long	                     waitTimeMillis;

	private final MBeanExportOperations	     mbeanExporter;

	private final ConcurrentMap<ID, Message>	messageReferenceToMessage;

	private final Semaphore	                 availableWindows;

	private volatile boolean	             shutDown	= false;

	public IncomingWindowStore(final int maximumCapacity,
	        final long waitTimeMillis, final MBeanExportOperations mbeanExporter) {
		notNull(mbeanExporter, "Argument 'mbeanExporter' must not be null");
		this.maximumCapacity = maximumCapacity;
		this.availableWindows = new Semaphore(maximumCapacity);
		this.waitTimeMillis = waitTimeMillis;
		this.mbeanExporter = mbeanExporter;
		this.messageReferenceToMessage = new ConcurrentHashMap<ID, Message>(
		        maximumCapacity);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getMaximumCapacity()
	 */
	@ManagedAttribute(description = "The maximum number of messages this store will buffer")
	public final int getMaximumCapacity() {
		return this.maximumCapacity;
	}

	@ManagedAttribute(description = "When trying to acquire a free window/slot for an incoming message we will "
	        + "wait for at most this number of milliseconds for a window to become available")
	public long getWaitTimeMillis() {
		return this.waitTimeMillis;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStoreMBean#getCurrentMessageCount()
	 */
	@ManagedAttribute(description = "Number of messages currently stored")
	public final int getCurrentMessageCount() {
		return this.messageReferenceToMessage.size();
	}

	public void attachTo(final Channel channel)
	        throws MalformedObjectNameException {
		notNull(channel, "Argument 'channel' must not be null");

		final ObjectName objectName = objectNameFor(channel);
		this.mbeanExporter.registerManagedResource(this, objectName);
		this.log.info("Registered {} with MBeanServer using ObjectName [{}]",
		        new Object[] { this, objectName });

		channel.getCloseFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future)
			        throws Exception {
				detachFrom(future.getChannel());
			}
		});
	}

	private final ObjectName objectNameFor(final Channel channel)
	        throws MalformedObjectNameException {
		return new ObjectName(
		        "vnet.sms:service=IncomingWindowStore,owner=channel-"
		                + channel.getId());
	}

	private void detachFrom(final Channel channel)
	        throws MalformedObjectNameException {
		notNull(channel, "Argument 'channel' must not be null");
		final ObjectName objectName = objectNameFor(channel);
		this.mbeanExporter.unregisterManagedResource(objectName);
		this.log.info("Unregistered {} from MBeanServer using ObjectName [{}]",
		        new Object[] { this, objectName });
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
