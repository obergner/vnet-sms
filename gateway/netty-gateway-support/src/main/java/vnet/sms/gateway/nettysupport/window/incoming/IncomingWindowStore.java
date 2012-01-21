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
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedNotification;
import org.springframework.jmx.export.annotation.ManagedNotifications;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.Jmx;

/**
 * @author obergner
 * 
 */
@ManagedNotifications({ @ManagedNotification(name = IncomingWindowStore.Events.NO_WINDOW_FOR_INCOMING_MESSAGE, description = "An incoming message has been rejected since no free windows has been available", notificationTypes = IncomingWindowStore.Events.NO_WINDOW_FOR_INCOMING_MESSAGE) })
@ManagedResource(description = "Buffers incoming messages until a predefined limit is reached.")
public class IncomingWindowStore<ID extends Serializable> implements
        NotificationPublisherAware {

	private static final String	TYPE	= "Channel";

	public static class Events {

		public static final String	NO_WINDOW_FOR_INCOMING_MESSAGE	= "channel.no-window-for-incoming-message";
	}

	private final Logger	                 log	              = LoggerFactory
	                                                                      .getLogger(getClass());

	private final int	                     maximumCapacity;

	private final long	                     waitTimeMillis;

	private final MBeanExportOperations	     mbeanExporter;

	private NotificationPublisher	         notificationPublisher;

	private final ConcurrentMap<ID, Message>	messageReferenceToMessage;

	private final Semaphore	                 availableWindows;

	private volatile boolean	             shutDown	          = false;

	private final AtomicLong	             noWindowAvailableSeq	= new AtomicLong(
	                                                                      1);

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

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

	// ------------------------------------------------------------------------
	// NotificationPublisherAware
	// ------------------------------------------------------------------------

	@Override
	public void setNotificationPublisher(
	        final NotificationPublisher notificationPublisher) {
		notNull(notificationPublisher,
		        "Argument 'notificationPublisher' must not be null");
		this.notificationPublisher = notificationPublisher;
	}

	// ------------------------------------------------------------------------
	// JMX API
	// ------------------------------------------------------------------------

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

	// ------------------------------------------------------------------------
	// Attach to/detach from Channel
	// ------------------------------------------------------------------------

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
		return new ObjectName(Jmx.GROUP + ":type=" + TYPE + ",scope="
		        + channel.getId() + ",name=incoming-windows");
	}

	private void detachFrom(final Channel channel)
	        throws MalformedObjectNameException {
		notNull(channel, "Argument 'channel' must not be null");
		final ObjectName objectName = objectNameFor(channel);
		this.mbeanExporter.unregisterManagedResource(objectName);
		this.log.info("Unregistered {} from MBeanServer using ObjectName [{}]",
		        new Object[] { this, objectName });
	}

	// ------------------------------------------------------------------------
	// Store new message
	// ------------------------------------------------------------------------

	/**
	 * @param messageEvent
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public boolean tryAcquireWindow(
	        final WindowedMessageEvent<ID, ? extends Message> messageEvent)
	        throws IllegalArgumentException, IllegalStateException,
	        InterruptedException {
		notNull(messageEvent, "Cannot store a null message");
		isTrue(messageEvent.getMessage() instanceof Message,
		        "Can only process MessageEvents containing a message of type "
		                + Message.class.getName() + ". Got: "
		                + messageEvent.getMessage());
		ensureNotShutDown();
		if (!this.availableWindows.tryAcquire(this.waitTimeMillis,
		        TimeUnit.MILLISECONDS)) {
			publishNoWindowAvailableEvent(messageEvent);
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

	private void publishNoWindowAvailableEvent(
	        final WindowedMessageEvent<ID, ? extends Message> messageEvent) {
		final Notification noWindowAvailableNot = new Notification(
		        Events.NO_WINDOW_FOR_INCOMING_MESSAGE, this,
		        this.noWindowAvailableSeq.getAndIncrement());
		noWindowAvailableNot.setUserData(messageEvent.getMessage());
		sendNotification(noWindowAvailableNot);
	}

	private void sendNotification(final Notification notification) {
		if (this.notificationPublisher == null) {
			throw new IllegalStateException(
			        "No "
			                + NotificationPublisher.class.getName()
			                + " has been set. Did you remember to manually inject a NotificationPublisher when using this class outside a Spring context?");
		}
		this.notificationPublisher.sendNotification(notification);
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

	// ------------------------------------------------------------------------
	// Release message from store
	// ------------------------------------------------------------------------

	/**
	 * @param messageReference
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	public Message releaseWindow(final ID messageReference)
	        throws IllegalArgumentException, IllegalStateException {
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

	// ------------------------------------------------------------------------
	// Shutdown this store
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
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
