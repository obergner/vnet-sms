/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.acknowledge.SendMessageAcknowledgementEvent;
import vnet.sms.gateway.nettysupport.ChannelUtils;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class WindowingChannelHandler<ID extends Serializable> extends
        SimpleChannelHandler {

	public static final String	          NAME	= "vnet.sms.gateway:incoming-outgoing-windowing-handler";

	private final Logger	              log	= LoggerFactory
	                                                   .getLogger(getClass());

	private final MetricsRegistry	      metricsRegistry;

	private final IncomingWindowStore<ID>	incomingWindowStore;

	private Gauge<Integer>	              maximumWindowCapacity;

	private Gauge<Long>	                  maxWindowWaitTimeMillis;

	private Gauge<Integer>	              currentlyUsedWindows;

	public WindowingChannelHandler(
	        final IncomingWindowStore<ID> incomingWindowStore,
	        final MetricsRegistry metricsRegistry) {
		notNull(incomingWindowStore,
		        "Argument 'incomingWindowStore' cannot be null");
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.incomingWindowStore = incomingWindowStore;
		this.metricsRegistry = metricsRegistry;
	}

	// ------------------------------------------------------------------------
	// Publish metrics
	// ------------------------------------------------------------------------

	/**
	 * @return the maximumWindowCapacity
	 */
	public final Gauge<Integer> getMaximumWindowCapacity() {
		return this.maximumWindowCapacity;
	}

	/**
	 * @return the maxWindowWaitTimeMillis
	 */
	public final Gauge<Long> getMaxWindowWaitTimeMillis() {
		return this.maxWindowWaitTimeMillis;
	}

	/**
	 * @return the currentlyUsedWindows
	 */
	public final Gauge<Integer> getCurrentlyUsedWindows() {
		return this.currentlyUsedWindows;
	}

	// ------------------------------------------------------------------------
	// Store incoming windowed messages in window store
	// ------------------------------------------------------------------------

	@Override
	public final void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e instanceof WindowedMessageEvent)) {
			throw new IllegalStateException("Unsupported MessageEvent type: "
			        + e);
		}
		windowedMessageReceived(ctx,
		        (WindowedMessageEvent<ID, ? extends GsmPdu>) e);
	}

	private void windowedMessageReceived(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends GsmPdu> e)
	        throws IllegalArgumentException, InterruptedException {
		this.log.trace("Processing {} ...", e);
		if (this.incomingWindowStore.tryAcquireWindow(e)) {
			this.log.trace("Acquired free window for {}", e);
			ctx.sendUpstream(e);
		} else {
			this.log.warn(
			        "No free window for {} available after waiting for {} milliseconds",
			        e, this.incomingWindowStore.getWaitTimeMillis());
			ctx.sendUpstream(new NoWindowForIncomingMessageAvailableEvent(
			        (UpstreamMessageEvent) e, this.incomingWindowStore
			                .getMaximumCapacity(), this.incomingWindowStore
			                .getWaitTimeMillis()));
		}
	}

	// ------------------------------------------------------------------------
	// Release previously stored windowed messages when receiving Acks/Nacks
	// ------------------------------------------------------------------------

	/**
	 * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		this.log.debug("Processing {} ...", e);
		if (e instanceof SendMessageAcknowledgementEvent) {
			final SendMessageAcknowledgementEvent<ID, ? extends GsmPdu> acknowledgedEvent = (SendMessageAcknowledgementEvent<ID, ? extends GsmPdu>) e;
			releaseAcknowledgedMessage(ctx, acknowledgedEvent);
			ctx.sendDownstream(acknowledgedEvent);
		} else {
			super.writeRequested(ctx, e);
		}
		this.log.debug("Finished processing {}", e);
	}

	private void releaseAcknowledgedMessage(
	        final ChannelHandlerContext ctx,
	        final SendMessageAcknowledgementEvent<ID, ? extends GsmPdu> acknowledgedEvent) {
		try {
			final GsmPdu acknowledgedMessage = acknowledgedEvent.getMessage();
			this.log.debug(
			        "Received acknowledgement {} for message {} - will release said message from incoming windowing store",
			        acknowledgedEvent.getAcknowledgement(), acknowledgedMessage);
			final ID acknowledgedMessageRef = acknowledgedEvent
			        .getAcknowledgedMessageReference();
			final GsmPdu releasedMessage = this.incomingWindowStore
			        .releaseWindow(acknowledgedMessageRef);
			if (!acknowledgedMessage.equals(releasedMessage)) {
				throw new IllegalArgumentException(
				        "The acknowledged message "
				                + acknowledgedMessage
				                + " is not the message "
				                + releasedMessage
				                + " that has been stored in this incoming windowing store under the same message reference ["
				                + acknowledgedMessageRef + "]");
			}
			this.log.debug("Released message {} from incoming windowing store",
			        releasedMessage);
		} catch (final Exception ex) {
			this.log.error("Failed to release acknowledged message "
			        + acknowledgedEvent.getMessage()
			        + " from incoming windowing store: " + ex.getMessage(), ex);
			ctx.sendUpstream(FailedToReleaseAcknowledgedMessageEvent.fail(
			        acknowledgedEvent, ex));
		}
	}

	// ------------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------------

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.maximumWindowCapacity = this.metricsRegistry.newGauge(
		        maximumCapacityMetricName(e.getChannel()),
		        new Gauge<Integer>() {
			        @Override
			        public Integer value() {
				        return WindowingChannelHandler.this.incomingWindowStore
				                .getMaximumCapacity();
			        }
		        });
		this.maxWindowWaitTimeMillis = this.metricsRegistry.newGauge(
		        maxWindowWaitTimeMillisMetricName(e.getChannel()),
		        new Gauge<Long>() {
			        @Override
			        public Long value() {
				        return WindowingChannelHandler.this.incomingWindowStore
				                .getWaitTimeMillis();
			        }
		        });
		this.currentlyUsedWindows = this.metricsRegistry.newGauge(
		        currentlyUsedWindowsMetricName(e.getChannel()),
		        new Gauge<Integer>() {
			        @Override
			        public Integer value() {
				        return WindowingChannelHandler.this.incomingWindowStore
				                .getCurrentMessageCount();
			        }
		        });

		super.channelConnected(ctx, e);
	}

	private MetricName currentlyUsedWindowsMetricName(final Channel channel) {
		return new MetricName(Channel.class, "currently-used-windows",
		        ChannelUtils.toString(channel));
	}

	private MetricName maxWindowWaitTimeMillisMetricName(final Channel channel) {
		return new MetricName(Channel.class, "max-window-wait-time-millis",
		        ChannelUtils.toString(channel));
	}

	private MetricName maximumCapacityMetricName(final Channel channel) {
		return new MetricName(Channel.class, "maximum-windows-capacity",
		        ChannelUtils.toString(channel));
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.metricsRegistry.removeMetric(maximumCapacityMetricName(e
		        .getChannel()));
		this.metricsRegistry.removeMetric(maxWindowWaitTimeMillisMetricName(e
		        .getChannel()));
		this.metricsRegistry.removeMetric(currentlyUsedWindowsMetricName(e
		        .getChannel()));

		final Map<ID, GsmPdu> pendingMessages = this.incomingWindowStore
		        .shutDown();
		if (!pendingMessages.isEmpty()) {
			this.log.warn(
			        "Channel {} has been disconnected while {} messages still await acknowledgement - these messages will be DISCARDED",
			        ctx.getChannel(), pendingMessages.size());
			final PendingWindowedMessagesDiscardedEvent<ID> pendingMessagesDiscarded = new PendingWindowedMessagesDiscardedEvent<ID>(
			        ctx.getChannel(), pendingMessages);
			ctx.sendUpstream(pendingMessagesDiscarded);
		}

		super.channelDisconnected(ctx, e);
	}
}
