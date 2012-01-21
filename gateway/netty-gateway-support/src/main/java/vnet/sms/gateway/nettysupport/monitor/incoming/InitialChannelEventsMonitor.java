package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Notification;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedNotification;
import org.springframework.jmx.export.annotation.ManagedNotifications;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import vnet.sms.gateway.nettysupport.Jmx;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = InitialChannelEventsMonitor.OBJECT_NAME, description = "Monitors and publishes initial channel events OPEN, BOUND, CONNECTED")
@ManagedNotifications({
        @ManagedNotification(name = InitialChannelEventsMonitor.Events.OPENED, description = "A channel has been opened. It is not yet bound to a local address.", notificationTypes = InitialChannelEventsMonitor.Events.OPENED),
        @ManagedNotification(name = InitialChannelEventsMonitor.Events.BOUND, description = "A channel has been bound to a local address. It is not yet connected to a remote address.", notificationTypes = InitialChannelEventsMonitor.Events.BOUND),
        @ManagedNotification(name = InitialChannelEventsMonitor.Events.CONNECTED, description = "A channel has been connected to a remote address. It is ready to receive messages.", notificationTypes = InitialChannelEventsMonitor.Events.CONNECTED) })
public class InitialChannelEventsMonitor implements NotificationPublisherAware {

	private static final String	TYPE	    = "Channels";

	private static final String	NAME	    = "initial-channel-events";

	static final String	        OBJECT_NAME	= Jmx.GROUP + ":type=" + TYPE
	                                                + ",name=" + NAME;

	public static class Events {

		public static final String	OPENED		= "channel.opened";

		public static final String	BOUND		= "channel.bound";

		public static final String	CONNECTED	= "channel.connected";
	}

	private final Logger	      log	                              = LoggerFactory
	                                                                          .getLogger(getClass());

	private NotificationPublisher	notificationPublisher;

	private final AtomicLong	  channelOpenedEventSequenceNumber	  = new AtomicLong(
	                                                                          1L);

	private final AtomicLong	  channelBoundEventSequenceNumber	  = new AtomicLong(
	                                                                          1L);

	private final AtomicLong	  channelConnectedEventSequenceNumber	= new AtomicLong(
	                                                                          1L);

	public InitialChannelEventsMonitor() {
		// Noop
	}

	public InitialChannelEventsMonitor(
	        final NotificationPublisher notificationPublisher) {
		notNull(notificationPublisher,
		        "Argument 'notificationPublisher' must not be null");
		this.notificationPublisher = notificationPublisher;
	}

	@Override
	public void setNotificationPublisher(
	        final NotificationPublisher notificationPublisher) {
		notNull(notificationPublisher,
		        "Argument 'notificationPublisher' must not be null");
		this.notificationPublisher = notificationPublisher;
	}

	void channelOpened(final Channel channel) {
		this.log.info("Channel {} has been opened", channel);
		final Notification notification = new Notification(Events.OPENED,
		        channel,
		        this.channelOpenedEventSequenceNumber.getAndIncrement(),
		        "Channel " + channel + " has been opened.");
		sendNotification(notification);
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

	void channelBound(final Channel channel, final SocketAddress localAddress) {
		this.log.info("Channel {} has been bound to local address [{}]",
		        channel, localAddress);
		final Notification notification = new Notification(Events.BOUND,
		        channel,
		        this.channelBoundEventSequenceNumber.getAndIncrement(),
		        "Channel " + channel + " has been bound to local address "
		                + localAddress);
		notification.setUserData(localAddress);
		sendNotification(notification);
	}

	void channelConnected(final Channel channel,
	        final SocketAddress remoteAddress) {
		this.log.info("Channel {} has been connected to remote address [{}]",
		        channel, remoteAddress);
		final Notification notification = new Notification(Events.CONNECTED,
		        channel,
		        this.channelConnectedEventSequenceNumber.getAndIncrement(),
		        "Channel " + channel + " has been connected to remote address "
		                + remoteAddress);
		notification.setUserData(remoteAddress);
		sendNotification(notification);
	}
}
