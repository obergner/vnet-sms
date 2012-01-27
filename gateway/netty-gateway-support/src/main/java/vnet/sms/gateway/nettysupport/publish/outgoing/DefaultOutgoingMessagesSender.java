/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.send.SendSmsContainer;
import vnet.sms.gateway.nettysupport.Jmx;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = DefaultOutgoingMessagesSender.OBJECT_NAME)
public class DefaultOutgoingMessagesSender<ID extends Serializable> implements
        OutgoingMessagesSender<ID> {

	// ------------------------------------------------------------------------
	// Static
	// ------------------------------------------------------------------------

	private static final String	                       TYPE	           = "OutgoingMessagesSender";

	private static final String	                       NAME	           = "DEFAULT";

	static final String	                               OBJECT_NAME	   = Jmx.GROUP
	                                                                           + ":type="
	                                                                           + TYPE
	                                                                           + ",name="
	                                                                           + NAME;

	// ------------------------------------------------------------------------
	// Instance
	// ------------------------------------------------------------------------

	private final Logger	                           log	           = LoggerFactory
	                                                                           .getLogger(getClass());

	private final Set<OutgoingMessagesSender.Listener>	listeners	   = new CopyOnWriteArraySet<OutgoingMessagesSender.Listener>();

	private final Meter	                               numberOfSentSms	= Metrics
	                                                                           .newMeter(
	                                                                                   new MetricName(
	                                                                                           Jmx.GROUP,
	                                                                                           TYPE,
	                                                                                           "sms-send-count"),
	                                                                                   "sms-sent",
	                                                                                   TimeUnit.SECONDS);

	private final Timer	                               smsSendDuration	= Metrics
	                                                                           .newTimer(
	                                                                                   new MetricName(
	                                                                                           Jmx.GROUP,
	                                                                                           TYPE,
	                                                                                           "sms-send-duration"),
	                                                                                   TimeUnit.MILLISECONDS,
	                                                                                   TimeUnit.SECONDS);

	private final ChannelGroup	                       allConnectedChannels;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * @param allConnectedChannels
	 */
	public DefaultOutgoingMessagesSender(final ChannelGroup allConnectedChannels) {
		notNull(allConnectedChannels,
		        "Argument 'allConnectedChannels' must not be null");
		this.allConnectedChannels = allConnectedChannels;
	}

	// ------------------------------------------------------------------------
	// Managing listeners
	// ------------------------------------------------------------------------

	@Override
	public boolean addListener(
	        final vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender.Listener listener) {
		this.log.info("Added listener {}", listener);
		return this.listeners.add(listener);
	}

	@Override
	public boolean removeListener(
	        final vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender.Listener listener) {
		this.log.info("Removed listener {}", listener);
		return this.listeners.remove(listener);
	}

	@Override
	public void clearListeners() {
		this.log.info("Clearing [{}] listeners", this.listeners.size());
		this.listeners.clear();
	}

	// ------------------------------------------------------------------------
	// API
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender#sendSms(vnet.sms.common.wme.send.SendSmsContainer)
	 */
	@Override
	public ChannelFuture sendSms(final SendSmsContainer sms) throws Exception {
		notNull(sms, "Argument 'sms' must not be null");
		try {
			this.log.debug("Sending {} ...");

			final Channel channel = selectRandomChannel();
			this.log.debug("Will send {} via {}", sms, channel);

			final TimerContext smsSendTimer = this.smsSendDuration.time();
			final ChannelFuture smsHasBeenSent = channel.write(sms);
			smsHasBeenSent.addListener(new SendSmsFuture(sms, smsSendTimer));

			return smsHasBeenSent;
		} catch (final Exception e) {
			fireSmsSendFailed(sms, e);
			throw e;
		}
	}

	private Channel selectRandomChannel() throws IllegalStateException {
		try {
			return this.allConnectedChannels.iterator().next();
		} catch (final NoSuchElementException e) {
			throw new IllegalStateException(
			        "No channel is currently connected - cannot determine channel via which to send message");
		}
	}

	private void fireSmsSendFailed(final SendSmsContainer failedSms,
	        final Throwable error) {
		for (final Listener listener : this.listeners) {
			listener.sendSmsFailed(failedSms, error);
		}
	}

	private final class SendSmsFuture implements ChannelFutureListener {
		private final SendSmsContainer	sms;
		private final TimerContext		smsSendTimer;

		private SendSmsFuture(final SendSmsContainer sms,
		        final TimerContext smsSendTimer) {
			this.sms = sms;
			this.smsSendTimer = smsSendTimer;
		}

		@Override
		public void operationComplete(final ChannelFuture future)
		        throws Exception {
			this.smsSendTimer.stop();
			if (!future.isSuccess()) {
				DefaultOutgoingMessagesSender.this.log.error(
				        "Sending {} failed: " + future.getCause().getMessage(),
				        future.getCause());
				fireSmsSendFailed(this.sms, future.getCause());
			} else {
				DefaultOutgoingMessagesSender.this.log.debug(
				        "Successfully sent {} via {}", this.sms,
				        future.getChannel());
			}
		}
	}

	// ------------------------------------------------------------------------
	// JMX API
	// ------------------------------------------------------------------------

	// FIXME: This method does currently fill neither originator nor destination
	// MSISDN
	public void sendSms(final String text) throws Exception {
		this.log.info("Received request to send SMS [\"{}\"] via JMX", text);
		final Sms sms = new Sms(text, new InetSocketAddress(0),
		        new InetSocketAddress(1));
		final SendSmsContainer sendSmsContainer = new SendSmsContainer(sms);
		final ChannelFuture smsSent = sendSms(sendSmsContainer);
		this.log.info(
		        "{} has been sent asynchronously - will await successful sent",
		        sms);
		smsSent.awaitUninterruptibly();
	}

	// ------------------------------------------------------------------------
	// Clean up resources
	// ------------------------------------------------------------------------

	@PreDestroy
	public void close() {
		this.log.info("Closing OutgoingMessagesSender - will remove all statistics from MBean server ...");
		Metrics.defaultRegistry().removeMetric(
		        metricNameOf(this.numberOfSentSms));
		Metrics.defaultRegistry().removeMetric(
		        metricNameOf(this.smsSendDuration));
		this.log.info("OutgoingMessagesSender closed - all statistics have been removed from MBean server");
	}

	private MetricName metricNameOf(final Metric metric) {
		for (final Map.Entry<MetricName, Metric> namePlusMetric : Metrics
		        .defaultRegistry().allMetrics().entrySet()) {
			if (namePlusMetric.getValue().equals(metric)) {
				return namePlusMetric.getKey();
			}
		}
		throw new IllegalArgumentException("Metric [" + metric
		        + "] has not been registered in MetricsRegistry ["
		        + Metrics.defaultRegistry() + "]");
	}
}
