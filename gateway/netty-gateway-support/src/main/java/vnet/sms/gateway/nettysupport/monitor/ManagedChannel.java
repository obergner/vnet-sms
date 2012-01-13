/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notNull;

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
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

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
@ManagedNotifications({
        @ManagedNotification(name = ManagedChannel.Events.CHANNEL_AUTHENTICATED, description = "Channel has been authenticated", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATED),
        @ManagedNotification(name = ManagedChannel.Events.CHANNEL_AUTHENTICATION_FAILED, description = "Attempt to authenticate channel failed", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATION_FAILED),
        @ManagedNotification(name = ManagedChannel.Events.STARTED_TO_PING, description = "Started to send out Pings", notificationTypes = ManagedChannel.Events.STARTED_TO_PING),
        @ManagedNotification(name = ManagedChannel.Events.PING_RESPONSE_TIMEOUT_EXPIRED, description = "Did not receive a response to a previously sent out Ping within the predefined timeout", notificationTypes = ManagedChannel.Events.PING_RESPONSE_TIMEOUT_EXPIRED),
        @ManagedNotification(name = ManagedChannel.Events.NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE, description = "A message came in, but no free window was available", notificationTypes = ManagedChannel.Events.NO_WINDOW_FOR_INCOMING_MESSAGE_AVAILABLE),
        @ManagedNotification(name = ManagedChannel.Events.PENDING_WINDOWED_MESSAGES_DISCARDED, description = "Messages stored in window store have expired and will be discarded", notificationTypes = ManagedChannel.Events.CHANNEL_AUTHENTICATED) })
@ManagedResource
public class ManagedChannel implements NotificationPublisherAware {

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
					managedChannel.close();
				}
			});

			managedChannel.addChannelMonitorToChannel();

			this.mbeanExporter.registerManagedResource(managedChannel,
			        managedChannel.getObjectName());

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

	private final ChannelMonitor	    listener;

	private ManagedChannel(final Channel channel,
	        final MBeanExportOperations mbeanExporter) {
		notNull(channel, "Argument 'channel' must not be null");
		notNull(mbeanExporter, "Argument 'mbeanExporter' must not be null");

		this.channel = channel;
		this.listener = this.new Listener();
		this.mbeanExporter = mbeanExporter;
		// Incoming metrics
		this.numberOfReceivedBytes = Metrics.newHistogram(Channel.class,
		        "received-bytes", channel.getId().toString());
		this.totalNumberOfReceivedBytes = Metrics.newCounter(Channel.class,
		        "total-received-bytes", channel.getId().toString());
		this.numberOfReceivedPdus = Metrics.newMeter(Channel.class,
		        "received-pdus", channel.getId().toString(), "pdu-received",
		        TimeUnit.SECONDS);
		this.numberOfReceivedLoginRequests = Metrics.newMeter(Channel.class,
		        "received-login-requests", channel.getId().toString(),
		        "login-request-received", TimeUnit.SECONDS);
		this.numberOfReceivedLoginResponses = Metrics.newMeter(Channel.class,
		        "received-login-responses", channel.getId().toString(),
		        "login-response-received", TimeUnit.SECONDS);
		this.numberOfReceivedPingRequests = Metrics.newMeter(Channel.class,
		        "received-ping-requests", channel.getId().toString(),
		        "pdu-received", TimeUnit.SECONDS);
		this.numberOfReceivedPingResponses = Metrics.newMeter(Channel.class,
		        "received-ping-responses", channel.getId().toString(),
		        "ping-response-received", TimeUnit.SECONDS);
		this.numberOfReceivedSms = Metrics.newMeter(Channel.class,
		        "received-sms", channel.getId().toString(), "pdu-received",
		        TimeUnit.SECONDS);
		// Outgoing metrics
		this.numberOfSentBytes = Metrics.newHistogram(Channel.class,
		        "sent-bytes", channel.getId().toString());
		this.totalNumberOfSentBytes = Metrics.newCounter(Channel.class,
		        "total-sent-bytes", channel.getId().toString());
		this.numberOfSentPdus = Metrics.newMeter(Channel.class, "sent-pdus",
		        channel.getId().toString(), "pdu-sent", TimeUnit.SECONDS);
		this.numberOfAcceptedLoginRequests = Metrics.newMeter(Channel.class,
		        "accepted-login-requests", channel.getId().toString(),
		        "login-request-accepted", TimeUnit.SECONDS);
		this.numberOfRejectedLoginRequests = Metrics.newMeter(Channel.class,
		        "rejected-login-requests", channel.getId().toString(),
		        "login-request-rejected", TimeUnit.SECONDS);
		this.numberOfSentPingRequests = Metrics.newMeter(Channel.class,
		        "sent-ping-requests", channel.getId().toString(),
		        "ping-request-sent", TimeUnit.SECONDS);
		this.numberOfSentPingResponses = Metrics.newMeter(Channel.class,
		        "sent-ping-responses", channel.getId().toString(),
		        "ping-response-sent", TimeUnit.SECONDS);
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

	void close() {
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

		this.mbeanExporter.unregisterManagedResource(getObjectName());
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

	private ObjectName getObjectName() {
		try {
			return new ObjectName(
			        "vnet.sms.gateway.netty-gateway-support:component=Channel,id="
			                + this.channel.getId());
		} catch (final MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------------------------------------------------
	//
	// ------------------------------------------------------------------------

	@Override
	public void setNotificationPublisher(
	        final NotificationPublisher notificationPublisher) {
		notNull(notificationPublisher,
		        "Argument 'notificationPublisher' must not be null");
		this.notificationPublisher = notificationPublisher;
	}

	// ------------------------------------------------------------------------
	// number of received bytes
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of bytes received since this channel has been connected")
	public long getTotalNumberOfReceivedBytes() {
		return this.totalNumberOfReceivedBytes.count();
	}

	@ManagedAttribute(description = "Mean number of bytes received per frame")
	public double getMeanNumberOfReceivedBytes() {
		return this.numberOfReceivedBytes.mean();
	}

	@ManagedAttribute(description = "Maximum number of bytes received per frame")
	public double getMaxNumberOfReceivedBytes() {
		return this.numberOfReceivedBytes.max();
	}

	@ManagedAttribute(description = "Minimum number of bytes received per frame")
	public double getMinNumberOfReceivedBytes() {
		return this.numberOfReceivedBytes.min();
	}

	@ManagedAttribute(description = "Standard deviation of bytes received per frame")
	public double getNumberOfReceivedBytesStdDev() {
		return this.numberOfReceivedBytes.stdDev();
	}

	@ManagedOperation(description = "Compute and return percentile of number of bytes received per frame")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "percentile", description = "The value returned from this operation will denote the upper bound "
	        + "of bytes per frame for 'percentile' percent of all received frames") })
	public double numberOfReceivedBytesPercentile(final double percentile) {
		return this.numberOfReceivedBytes.percentile(percentile);
	}

	// ------------------------------------------------------------------------
	// number of received PDUs
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of PDUs received since this channel has been connected")
	public long getTotalNumberOfReceivedPdus() {
		return this.numberOfReceivedPdus.count();
	}

	@ManagedAttribute(description = "Number of PDUs received within the last minute")
	public double getNumberOfPdusReceivedWithinLast1Minute() {
		return this.numberOfReceivedPdus.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of PDUs received within the last 5 minutes")
	public double getNumberOfPdusReceivedWithinLast5Minutes() {
		return this.numberOfReceivedPdus.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of PDUs received within the last 15 minutes")
	public double getNumberOfPdusReceivedWithinLast15Minutes() {
		return this.numberOfReceivedPdus.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Mean number of PDUs received per second")
	public double getMeanNumberOfPdusReceivedPerSecond() {
		return this.numberOfReceivedPdus.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of accepted login requests
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of accepted/successful login requests")
	public long getTotalNumberOfAcceptedLoginRequests() {
		return this.numberOfAcceptedLoginRequests.count();
	}

	@ManagedAttribute(description = "Number of login requests accepted within the last minute")
	public double getNumberOfLoginRequestsAcceptedWithinLast1Minute() {
		return this.numberOfAcceptedLoginRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests accepted within the last 5 minutes")
	public double getNumberOfLoginRequestsAcceptedWithinLast5Minutes() {
		return this.numberOfAcceptedLoginRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests accepted within the last 15 minutes")
	public double getNumberOfLoginRequestsAcceptedWithinLast15Minutes() {
		return this.numberOfAcceptedLoginRequests.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of accepted login requests per second")
	public double getMeanNumberOfLoginRequestsAcceptedPerSecond() {
		return this.numberOfAcceptedLoginRequests.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of rejected login requests
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of rejected/failed login requests")
	public long getTotalNumberOfRejectedLoginRequests() {
		return this.numberOfRejectedLoginRequests.count();
	}

	@ManagedAttribute(description = "Number of login requests rejected within the last minute")
	public double getNumberOfLoginRequestsRejectedWithinLast1Minute() {
		return this.numberOfRejectedLoginRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests rejected within the last 5 minutes")
	public double getNumberOfLoginRequestsRejectedWithinLast5Minutes() {
		return this.numberOfRejectedLoginRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests rejected within the last 15 minutes")
	public double getNumberOfLoginRequestsRejectedWithinLast15Minutes() {
		return this.numberOfRejectedLoginRequests.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of rejected login requests per second")
	public double getMeanNumberOfLoginRequestsRejectedPerSecond() {
		return this.numberOfRejectedLoginRequests.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of sent bytes
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of bytes sent since this channel has been connected")
	public long getTotalNumberOfSentBytes() {
		return this.totalNumberOfSentBytes.count();
	}

	@ManagedAttribute(description = "Mean number of bytes sent per frame")
	public double getMeanNumberOfSentBytes() {
		return this.numberOfSentBytes.mean();
	}

	@ManagedAttribute(description = "Maximum number of bytes sent per frame")
	public double getMaxNumberOfSentBytes() {
		return this.numberOfSentBytes.max();
	}

	@ManagedAttribute(description = "Minimum number of bytes sent per frame")
	public double getMinNumberOfSentBytes() {
		return this.numberOfSentBytes.min();
	}

	@ManagedAttribute(description = "Standard deviation of bytes sent per frame")
	public double getNumberOfSentBytesStdDev() {
		return this.numberOfSentBytes.stdDev();
	}

	@ManagedOperation(description = "Compute and return percentile of number of bytes sent per frame")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "percentile", description = "The value returned from this operation will denote the upper bound "
	        + "of bytes per frame for 'percentile' percent of all sent frames") })
	public double numberOfSentBytesPercentile(final double percentile) {
		return this.numberOfSentBytes.percentile(percentile);
	}

	// ------------------------------------------------------------------------
	// number of sent PDUs
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of sent PDUs")
	public long getTotalNumberOfSentPdus() {
		return this.numberOfSentPdus.count();
	}

	@ManagedAttribute(description = "Number of PDUs sent within the last minute")
	public double getNumberOfPdusSentWithinLast1Minute() {
		return this.numberOfSentPdus.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of PDUs sent within the last 5 minutes")
	public double getNumberOfPdusSentWithinLast5Minutes() {
		return this.numberOfSentPdus.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of PDUs sent within the last 15 minutes")
	public double getNumberOfPdusSentWithinLast15Minutes() {
		return this.numberOfSentPdus.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of PDUs sent per second")
	public double getMeanNumberOfPdusSentPerSecond() {
		return this.numberOfSentPdus.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of sent ping requests
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of sent ping requests")
	public long getTotalNumberOfSentPingRequests() {
		return this.numberOfSentPingRequests.count();
	}

	@ManagedAttribute(description = "Number of ping requests sent within the last minute")
	public double getNumberOfPingRequestsSentWithinLast1Minute() {
		return this.numberOfSentPingRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping requests sent within the last 5 minutes")
	public double getNumberOfPingRequestsSentWithinLast5Minutes() {
		return this.numberOfSentPingRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping requests sent within the last 15 minutes")
	public double getNumberOfPingRequestsSentWithinLast15Minutes() {
		return this.numberOfSentPingRequests.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of rejected login requests per second")
	public double getMeanNumberOfPingRequestsSentPerSecond() {
		return this.numberOfSentPingRequests.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of sent ping responses
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of sent ping responses")
	public long getTotalNumberOfSentPingResponses() {
		return this.numberOfSentPingResponses.count();
	}

	@ManagedAttribute(description = "Number of ping responses sent within the last minute")
	public double getNumberOfPingResponsesSentWithinLast1Minute() {
		return this.numberOfSentPingResponses.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses sent within the last 5 minutes")
	public double getNumberOfPingResponsesSentWithinLast5Minutes() {
		return this.numberOfSentPingResponses.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses sent within the last 15 minutes")
	public double getNumberOfPingResponsesSentWithinLast15Minutes() {
		return this.numberOfSentPingResponses.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses sent per second")
	public double getMeanNumberOfPingResponsesSentPerSecond() {
		return this.numberOfSentPingResponses.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of received login requests
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of received login requests")
	public long getTotalNumberOfReceivedLoginRequests() {
		return this.numberOfReceivedLoginRequests.count();
	}

	@ManagedAttribute(description = "Number of login requests received within the last minute")
	public double getNumberOfLoginRequestsReceivedWithinLast1Minute() {
		return this.numberOfReceivedLoginRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests received within the last 5 minutes")
	public double getNumberOfLoginRequestsReceivedWithinLast5Minutes() {
		return this.numberOfReceivedLoginRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests received within the last 15 minutes")
	public double getNumberOfLoginRequestsReceivedWithinLast15Minutes() {
		return this.numberOfReceivedLoginRequests.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of login requests received per second")
	public double getMeanNumberOfLoginRequestsReceivedPerSecond() {
		return this.numberOfReceivedLoginRequests.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of received login responses
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of received login responses")
	public long getTotalNumberOfReceivedLoginResponses() {
		return this.numberOfReceivedLoginResponses.count();
	}

	@ManagedAttribute(description = "Number of login responses received within the last minute")
	public double getNumberOfLoginResponsesReceivedWithinLast1Minute() {
		return this.numberOfReceivedLoginResponses.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of login responses received within the last 5 minutes")
	public double getNumberOfLoginResponsesReceivedWithinLast5Minutes() {
		return this.numberOfReceivedLoginResponses.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of login responses received within the last 15 minutes")
	public double getNumberOfLoginResponsesReceivedWithinLast15Minutes() {
		return this.numberOfReceivedLoginResponses.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of login responses received per second")
	public double getMeanNumberOfLoginResponsesReceivedPerSecond() {
		return this.numberOfReceivedLoginResponses.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of received ping requests
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of received ping requests")
	public long getTotalNumberOfReceivedPingRequests() {
		return this.numberOfReceivedPingRequests.count();
	}

	@ManagedAttribute(description = "Number of ping requests received within the last minute")
	public double getNumberOfPingRequestsReceivedWithinLast1Minute() {
		return this.numberOfReceivedPingRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping requests received within the last 5 minutes")
	public double getNumberOfPingRequestsReceivedWithinLast5Minutes() {
		return this.numberOfReceivedPingRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping requests received within the last 15 minutes")
	public double getNumberOfPingRequestsReceivedWithinLast15Minutes() {
		return this.numberOfReceivedPingRequests.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping requests received per second")
	public double getMeanNumberOfPingRequestsReceivedPerSecond() {
		return this.numberOfReceivedPingRequests.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of received ping responses
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of received ping responses")
	public long getTotalNumberOfReceivedPingResponses() {
		return this.numberOfReceivedPingResponses.count();
	}

	@ManagedAttribute(description = "Number of ping responses received within the last minute")
	public double getNumberOfPingResponsesReceivedWithinLast1Minute() {
		return this.numberOfReceivedPingResponses.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses received within the last 5 minutes")
	public double getNumberOfPingResponsesReceivedWithinLast5Minutes() {
		return this.numberOfReceivedPingResponses.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses received within the last 15 minutes")
	public double getNumberOfPingResponsesReceivedWithinLast15Minutes() {
		return this.numberOfReceivedPingResponses.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of ping responses received per second")
	public double getMeanNumberOfPingResponsesReceivedPerSecond() {
		return this.numberOfReceivedPingResponses.meanRate();
	}

	// ------------------------------------------------------------------------
	// number of received SMS
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "Total number of received SMS")
	public long getTotalNumberOfReceivedSms() {
		return this.numberOfReceivedSms.count();
	}

	@ManagedAttribute(description = "Number of SMS received within the last minute")
	public double getNumberOfSmsReceivedWithinLast1Minute() {
		return this.numberOfReceivedSms.oneMinuteRate();
	}

	@ManagedAttribute(description = "Number of SMS received within the last 5 minutes")
	public double getNumberOfSmsReceivedWithinLast5Minutes() {
		return this.numberOfReceivedSms.fiveMinuteRate();
	}

	@ManagedAttribute(description = "Number of SMS received within the last 15 minutes")
	public double getNumberOfSmsReceivedWithinLast15Minutes() {
		return this.numberOfReceivedSms.fifteenMinuteRate();
	}

	@ManagedAttribute(description = "Number of SMS received per second")
	public double getMeanNumberOfSmsReceivedPerSecond() {
		return this.numberOfReceivedSms.meanRate();
	}

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

	@Override
	public String toString() {
		return "ManagedChannel@" + hashCode() + "[channel: " + this.channel
		        + "|numberOfReceivedBytes: " + getTotalNumberOfReceivedBytes()
		        + "|numberOfReceivedPdus: " + getTotalNumberOfReceivedPdus()
		        + "|numberOfReceivedLoginRequests: "
		        + getTotalNumberOfReceivedLoginRequests()
		        + "|numberOfReceivedLoginResponses: "
		        + getTotalNumberOfReceivedLoginResponses()
		        + "|numberOfReceivedPingRequests: "
		        + getTotalNumberOfReceivedPingRequests()
		        + "|numberOfReceivedPingResponses: "
		        + getTotalNumberOfReceivedPingResponses()
		        + "|numberOfReceivedSms: " + getTotalNumberOfReceivedSms()
		        + "|numberOfAcceptedLoginRequests: "
		        + getTotalNumberOfAcceptedLoginRequests()
		        + "|numberOfRejectedLoginRequests: "
		        + getTotalNumberOfRejectedLoginRequests()
		        + "|numberOfSentBytes: " + getTotalNumberOfSentBytes()
		        + "|numberOfSentPdus: " + getTotalNumberOfSentPdus()
		        + "|numberOfSentPingRequests: "
		        + getTotalNumberOfSentPingRequests()
		        + "|numberOfSentPingResponses: "
		        + getTotalNumberOfSentPingResponses() + "]";
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
