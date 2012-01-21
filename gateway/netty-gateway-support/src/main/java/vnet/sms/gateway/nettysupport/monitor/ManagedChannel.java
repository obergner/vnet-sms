/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedNotification;
import org.springframework.jmx.export.annotation.ManagedNotifications;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import vnet.sms.gateway.nettysupport.Jmx;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelAuthenticationFailedEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.PingResponseTimeoutExpiredEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.StartedToPingEvent;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettysupport.window.PendingWindowedMessagesDiscardedEvent;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

/**
 * @author obergner
 * 
 */
public class ManagedChannel {

	private static final String	TYPE	= "Channel";

	public static class Events {

		public static final String	CHANNEL_AUTHENTICATED		             = "channel.authenticated";

		public static final String	CHANNEL_AUTHENTICATION_FAILED		     = "channel.authentication-failed";

		public static final String	STARTED_TO_PING		                     = "channel.started-to-ping";

		public static final String	PING_RESPONSE_TIMEOUT_EXPIRED		     = "channel.ping-response-timeout-expired";

		public static final String	NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE	= "channel.no-window-for-incoming-message-available";

		public static final String	PENDING_WINDOWED_MESSAGES_DISCARDED		 = "channel.pending-windowed-messages-discarde";
	}

	static class Factory {

		private final MBeanExportOperations	mbeanExporter;

		/**
		 * @param mbeanExporter
		 */
		private Factory(final MBeanExportOperations mbeanExporter) {
			notNull(mbeanExporter, "Argument 'mbeanExporter' must not be null");
			this.mbeanExporter = mbeanExporter;
		}

		ManagedChannel attachTo(final Channel channel) {
			final ManagedChannel managedChannel = new ManagedChannel(channel,
			        this.mbeanExporter);

			channel.getCloseFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future)
				        throws Exception {
					managedChannel.cleanup();
				}
			});

			// Take care to call this AFTER managedChannel has been registered
			// in MBeanExporter since it will receive its NotificationPublisher
			// during that registration. Otherwise, it may receive events to
			// publish via JMX BEFORE it obtained its NotificationPublisher.
			managedChannel.addChannelMonitorToChannel();

			return managedChannel;
		}
	}

	public static ManagedChannel.Factory factory(
	        final MBeanExportOperations mbeanExporter) {
		return new ManagedChannel.Factory(mbeanExporter);
	}

	private final Logger	            log	= LoggerFactory
	                                                .getLogger(getClass());

	private final MBeanExportOperations	mbeanExporter;

	private NotificationPublisher	    notificationPublisher;

	private final HistogramMetric	    numberOfReceivedBytes;

	private final CounterMetric	        totalNumberOfReceivedBytes;

	private final MeterMetric	        numberOfReceivedPdus;

	private final MeterMetric	        numberOfReceivedLoginRequests;

	private final MeterMetric	        numberOfReceivedLoginResponses;

	private final MeterMetric	        numberOfReceivedPingRequests;

	private final MeterMetric	        numberOfReceivedPingResponses;

	private final MeterMetric	        numberOfReceivedSms;

	private final MeterMetric	        numberOfAcceptedLoginRequests;

	private final MeterMetric	        numberOfRejectedLoginRequests;

	private final HistogramMetric	    numberOfSentBytes;

	private final CounterMetric	        totalNumberOfSentBytes;

	private final MeterMetric	        numberOfSentPdus;

	private final MeterMetric	        numberOfSentPingRequests;

	private final MeterMetric	        numberOfSentPingResponses;

	private final Channel	            channel;

	private final Controller	        controller;

	private final ChannelMonitor	    listener;

	private final long	                connectedSinceTimestamp;

	private ManagedChannel(final Channel channel,
	        final MBeanExportOperations mbeanExporter) {
		notNull(channel, "Argument 'channel' must not be null");
		notNull(mbeanExporter, "Argument 'mbeanExporter' must not be null");

		this.connectedSinceTimestamp = System.currentTimeMillis();
		this.channel = channel;
		this.listener = this.new Listener();
		this.mbeanExporter = mbeanExporter;
		// Incoming metrics
		this.numberOfReceivedBytes = Metrics.newHistogram(new MetricName(
		        Jmx.GROUP, TYPE, "received-bytes", channel.getId().toString()));
		this.totalNumberOfReceivedBytes = Metrics.newCounter(new MetricName(
		        Jmx.GROUP, TYPE, "total-received-bytes", channel.getId()
		                .toString()));
		this.numberOfReceivedPdus = Metrics.newMeter(new MetricName(Jmx.GROUP,
		        TYPE, "received-pdus", channel.getId().toString()),
		        "pdu-received", TimeUnit.SECONDS);
		this.numberOfReceivedLoginRequests = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "received-login-requests", channel.getId()
		                .toString()), "login-request-received",
		        TimeUnit.SECONDS);
		this.numberOfReceivedLoginResponses = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "received-login-responses", channel.getId()
		                .toString()), "login-response-received",
		        TimeUnit.SECONDS);
		this.numberOfReceivedPingRequests = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "received-ping-requests", channel.getId()
		                .toString()), "pdu-received", TimeUnit.SECONDS);
		this.numberOfReceivedPingResponses = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "received-ping-responses", channel.getId()
		                .toString()), "ping-response-received",
		        TimeUnit.SECONDS);
		this.numberOfReceivedSms = Metrics.newMeter(new MetricName(Jmx.GROUP,
		        TYPE, "received-sms", channel.getId().toString()),
		        "pdu-received", TimeUnit.SECONDS);
		// Outgoing metrics
		this.numberOfSentBytes = Metrics.newHistogram(new MetricName(Jmx.GROUP,
		        TYPE, "sent-bytes", channel.getId().toString()));
		this.totalNumberOfSentBytes = Metrics
		        .newCounter(new MetricName(Jmx.GROUP, TYPE, "total-sent-bytes",
		                channel.getId().toString()));
		this.numberOfSentPdus = Metrics.newMeter(new MetricName(Jmx.GROUP,
		        TYPE, "sent-pdus", channel.getId().toString()), "pdu-sent",
		        TimeUnit.SECONDS);
		this.numberOfAcceptedLoginRequests = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "accepted-login-requests", channel.getId()
		                .toString()), "login-request-accepted",
		        TimeUnit.SECONDS);
		this.numberOfRejectedLoginRequests = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "rejected-login-requests", channel.getId()
		                .toString()), "login-request-rejected",
		        TimeUnit.SECONDS);
		this.numberOfSentPingRequests = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "sent-ping-requests", channel.getId()
		                .toString()), "ping-request-sent", TimeUnit.SECONDS);
		this.numberOfSentPingResponses = Metrics.newMeter(new MetricName(
		        Jmx.GROUP, TYPE, "sent-ping-responses", channel.getId()
		                .toString()), "ping-response-sent", TimeUnit.SECONDS);
		// Controller
		this.controller = this.new Controller();
		this.mbeanExporter.registerManagedResource(this.controller,
		        this.controller.getObjectName());
	}

	private void addChannelMonitorToChannel() throws IllegalArgumentException {
		final ChannelPipeline pipeline = this.channel.getPipeline();
		boolean monitorEnabledChannelHandlerFound = false;
		for (final ChannelHandler handler : pipeline.toMap().values()) {
			if (handler instanceof MonitoredChannel) {
				monitorEnabledChannelHandlerFound = true;
				final MonitoredChannel monitoringHandler = MonitoredChannel.class
				        .cast(handler);
				monitoringHandler.addMonitor(getMonitor());
				this.log.trace(
				        "Added channel monitor [{}] to channel handler [{}]",
				        getMonitor(), monitoringHandler);
			}
		}
		if (!monitorEnabledChannelHandlerFound) {
			throw new IllegalArgumentException(
			        "The pipeline ["
			                + pipeline
			                + "] attached to channel ["
			                + this.channel
			                + "] does not contain a ChannelHandler that implements ["
			                + MonitoredChannel.class.getName()
			                + "]. This ManagedChannel requires its Channel to be monitoring-enabled.");
		}
	}

	void cleanup() {
		Metrics.removeMetric(metricNameOf(this.numberOfAcceptedLoginRequests));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedBytes));
		Metrics.removeMetric(metricNameOf(this.totalNumberOfReceivedBytes));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedLoginRequests));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedLoginResponses));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedPdus));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedPingRequests));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedPingResponses));
		Metrics.removeMetric(metricNameOf(this.numberOfReceivedSms));
		Metrics.removeMetric(metricNameOf(this.numberOfRejectedLoginRequests));
		Metrics.removeMetric(metricNameOf(this.numberOfSentBytes));
		Metrics.removeMetric(metricNameOf(this.totalNumberOfSentBytes));
		Metrics.removeMetric(metricNameOf(this.numberOfSentPdus));
		Metrics.removeMetric(metricNameOf(this.numberOfSentPingRequests));
		Metrics.removeMetric(metricNameOf(this.numberOfSentPingResponses));

		removeChannelMonitorFromChannel();

		this.mbeanExporter.unregisterManagedResource(this.controller
		        .getObjectName());
		this.log.debug(
		        "Removed {} from MBeanServer as the underlying channel {} has been closed",
		        this, this.channel);
	}

	private MetricName metricNameOf(final Metric metric) {
		for (final Map.Entry<MetricName, Metric> namePlusMetric : Metrics
		        .allMetrics().entrySet()) {
			if (namePlusMetric.getValue().equals(metric)) {
				return namePlusMetric.getKey();
			}
		}
		throw new IllegalArgumentException("Metric [" + metric
		        + "] has not been registered in MetricsRegistry ["
		        + Metrics.defaultRegistry() + "]");
	}

	private void removeChannelMonitorFromChannel() {
		final ChannelPipeline pipeline = this.channel.getPipeline();
		for (final ChannelHandler handler : pipeline.toMap().values()) {
			if (handler instanceof MonitoredChannel) {
				final MonitoredChannel monitoringHandler = MonitoredChannel.class
				        .cast(handler);
				monitoringHandler.removeMonitor(getMonitor());
				this.log.trace(
				        "Removed channel monitor [{}] from channel handler [{}]",
				        getMonitor(), monitoringHandler);
			}
		}
	}

	private ChannelMonitor getMonitor() {
		return this.listener;
	}

	// ------------------------------------------------------------------------
	//
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// equals, hashCode, ...
	// ------------------------------------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.channel == null) ? 0 : this.channel.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ManagedChannel other = (ManagedChannel) obj;
		if (this.channel == null) {
			if (other.channel != null) {
				return false;
			}
		} else if (!this.channel.equals(other.channel)) {
			return false;
		}
		return true;
	}

	// ------------------------------------------------------------------------
	// Inner classes
	// ------------------------------------------------------------------------

	@ManagedNotifications({
	        @ManagedNotification(name = ManagedChannel.Events.CHANNEL_AUTHENTICATED, description = "Channel has been authenticated", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATED),
	        @ManagedNotification(name = ManagedChannel.Events.CHANNEL_AUTHENTICATION_FAILED, description = "Attempt to authenticate channel failed", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATION_FAILED),
	        @ManagedNotification(name = ManagedChannel.Events.STARTED_TO_PING, description = "Started to send out Pings", notificationTypes = ManagedChannel.Events.STARTED_TO_PING),
	        @ManagedNotification(name = ManagedChannel.Events.PING_RESPONSE_TIMEOUT_EXPIRED, description = "Did not receive a response to a previously sent out Ping within the predefined timeout", notificationTypes = ManagedChannel.Events.PING_RESPONSE_TIMEOUT_EXPIRED),
	        @ManagedNotification(name = ManagedChannel.Events.NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE, description = "A message came in, but no free window was available", notificationTypes = ManagedChannel.Events.NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE),
	        @ManagedNotification(name = ManagedChannel.Events.PENDING_WINDOWED_MESSAGES_DISCARDED, description = "Messages stored in window store have expired and will be discarded", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATED) })
	@ManagedResource
	public class Controller implements NotificationPublisherAware {

		@ManagedOperation(description = "Closes this channel - this channel cannot be reused afterwards")
		public void close() {
			ManagedChannel.this.log.info(
			        "Received request to close channel {} via JMX",
			        ManagedChannel.this.channel);
			final ChannelFuture closed = ManagedChannel.this.channel.close();
			closed.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future)
				        throws Exception {
					if (future.isSuccess()) {
						ManagedChannel.this.log.info(
						        "Channel {} has been closed",
						        future.getChannel());
					} else {
						ManagedChannel.this.log.warn("Failed to close channel "
						        + future.getChannel() + ": "
						        + future.getCause().getMessage(),
						        future.getCause());
					}
				}
			});
		}

		@ManagedAttribute(description = "This channel's unique id")
		public Integer getId() {
			return ManagedChannel.this.channel.getId();
		}

		@ManagedAttribute(description = "Since when this channel is connected")
		public Date getConnectedSince() {
			return new Date(ManagedChannel.this.connectedSinceTimestamp);
		}

		@ManagedAttribute(description = "The local address this channel is bound to")
		public SocketAddress getLocalAddress() {
			return ManagedChannel.this.channel.getLocalAddress();
		}

		@ManagedAttribute(description = "The remote address this channel is connected to")
		public SocketAddress getRemoteAddress() {
			return ManagedChannel.this.channel.getRemoteAddress();
		}

		@ManagedAttribute(description = "The timeout in milliseconds after that this channel will consider a connection attempt failed")
		public int getConnectTimeoutMillis() {
			return ManagedChannel.this.channel.getConfig()
			        .getConnectTimeoutMillis();
		}

		ObjectName getObjectName() {
			try {
				return new ObjectName(Jmx.GROUP + ":type=" + TYPE + ",scope="
				        + ManagedChannel.this.channel.getId()
				        + ",name=Controller");
			} catch (final MalformedObjectNameException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setNotificationPublisher(
		        final NotificationPublisher notificationPublisher) {
			notNull(notificationPublisher,
			        "Argument 'notificationPublisher' must not be null");
			ManagedChannel.this.notificationPublisher = notificationPublisher;
		}
	}

	private class Listener implements ChannelMonitor {

		private final AtomicLong	channelAuthenticatedSeq		          = new AtomicLong(
		                                                                          1L);

		private final AtomicLong	channelAuthenticationFailedSeq		  = new AtomicLong(
		                                                                          1L);

		private final AtomicLong	startedToPingSeq		              = new AtomicLong(
		                                                                          1L);

		private final AtomicLong	pingResponseTimeoutExpiredSeq		  = new AtomicLong(
		                                                                          1L);

		private final AtomicLong	noWindowForIncomingMessaeAvailableSeq	= new AtomicLong(
		                                                                          1L);

		private final AtomicLong	pendingWindowedMessagedDiscardedSeq		= new AtomicLong(
		                                                                          1L);

		@Override
		public void bytesReceived(final long numberOfBytes) {
			ManagedChannel.this.numberOfReceivedBytes.update(numberOfBytes);
			ManagedChannel.this.totalNumberOfReceivedBytes.inc(numberOfBytes);
		}

		@Override
		public void pduReceived() {
			ManagedChannel.this.numberOfReceivedPdus.mark();
		}

		@Override
		public void loginRequestReceived() {
			ManagedChannel.this.numberOfReceivedLoginRequests.mark();
		}

		@Override
		public void loginResponseReceived() {
			ManagedChannel.this.numberOfReceivedLoginResponses.mark();
		}

		@Override
		public void pingRequestReceived() {
			ManagedChannel.this.numberOfReceivedPingRequests.mark();
		}

		@Override
		public void pingResponseReceived() {
			ManagedChannel.this.numberOfReceivedPingResponses.mark();
		}

		@Override
		public void smsReceived() {
			ManagedChannel.this.numberOfReceivedSms.mark();
		}

		@Override
		public void sendLoginRequestAccepted() {
			ManagedChannel.this.numberOfAcceptedLoginRequests.mark();
		}

		@Override
		public void sendLoginRequestRejected() {
			ManagedChannel.this.numberOfRejectedLoginRequests.mark();
		}

		@Override
		public void sendPingRequest() {
			ManagedChannel.this.numberOfSentPingRequests.mark();
		}

		@Override
		public void sendPingResponse() {
			ManagedChannel.this.numberOfSentPingResponses.mark();
		}

		@Override
		public void sendSms() {
			// TODO Auto-generated method stub
		}

		@Override
		public void sendPdu() {
			ManagedChannel.this.numberOfSentPdus.mark();
		}

		@Override
		public void sendBytes(final long numberOfBytes) {
			ManagedChannel.this.numberOfSentBytes.update(numberOfBytes);
			ManagedChannel.this.totalNumberOfSentBytes.inc(numberOfBytes);
		}

		@Override
		public void channelAuthenticated(
		        final ChannelSuccessfullyAuthenticatedEvent e) {
			final Notification notification = new Notification(
			        Events.CHANNEL_AUTHENTICATED, e.getChannel(),
			        this.channelAuthenticatedSeq.getAndIncrement());
			notification.setUserData(e.getSuccessfulLoginRequest());
			sendNotification(notification);
		}

		private void sendNotification(final Notification notification) {
			if (ManagedChannel.this.notificationPublisher == null) {
				throw new IllegalStateException(
				        "No "
				                + NotificationPublisher.class.getName()
				                + " has been set. Did you remember to manually inject a NotificationPublisher when using this class outside a Spring context?");
			}
			ManagedChannel.this.notificationPublisher
			        .sendNotification(notification);
		}

		@Override
		public void channelAuthenticationFailed(
		        final ChannelAuthenticationFailedEvent e) {
			final Notification notification = new Notification(
			        Events.CHANNEL_AUTHENTICATION_FAILED, e.getChannel(),
			        this.channelAuthenticationFailedSeq.getAndIncrement());
			notification.setUserData(e.getFailedLoginRequest());
			sendNotification(notification);
		}

		@Override
		public void startedToPing(final StartedToPingEvent e) {
			final Notification notification = new Notification(
			        Events.STARTED_TO_PING, e.getChannel(),
			        this.startedToPingSeq.getAndIncrement());
			notification.setUserData(e.getPingIntervalSeconds());
			sendNotification(notification);
		}

		@Override
		public void pingResponseTimeoutExpired(
		        final PingResponseTimeoutExpiredEvent e) {
			final Notification notification = new Notification(
			        Events.PING_RESPONSE_TIMEOUT_EXPIRED, e.getChannel(),
			        this.pingResponseTimeoutExpiredSeq.getAndIncrement());
			notification.setUserData(e.getPingResponseTimeoutMillis());
			sendNotification(notification);
		}

		@Override
		public void noWindowForIncomingMessageAvailable(
		        final NoWindowForIncomingMessageAvailableEvent e) {
			final Notification notification = new Notification(
			        Events.NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE,
			        e.getChannel(),
			        this.noWindowForIncomingMessaeAvailableSeq
			                .getAndIncrement());
			notification.setUserData(e.getMessage());
			sendNotification(notification);
		}

		@Override
		public void pendingWindowedMessagesDiscarded(
		        final PendingWindowedMessagesDiscardedEvent<?> e) {
			final Notification notification = new Notification(
			        Events.PENDING_WINDOWED_MESSAGES_DISCARDED, e.getChannel(),
			        this.pendingWindowedMessagedDiscardedSeq.getAndIncrement());
			sendNotification(notification);
		}
	}
}
