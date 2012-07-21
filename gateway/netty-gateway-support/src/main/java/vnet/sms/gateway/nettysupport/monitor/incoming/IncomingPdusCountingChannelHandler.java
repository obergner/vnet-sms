/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import vnet.sms.gateway.nettysupport.ChannelUtils;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class IncomingPdusCountingChannelHandler<TP> extends
        SimpleChannelUpstreamHandler {

	public static final String	  NAME	= "vnet.sms.gateway:incoming-pdus-counting-handler";

	private final Class<TP>	      pduType;

	private final MetricsRegistry	metricsRegistry;

	private Meter	              numberOfReceivedPdus;

	public IncomingPdusCountingChannelHandler(final Class<TP> pduType,
	        final MetricsRegistry metricsRegistry) {
		notNull(pduType, "Argument 'pduType' must not be null");
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.pduType = pduType;
		this.metricsRegistry = metricsRegistry;
	}

	/**
	 * @return the numberOfReceivedPdus
	 */
	public final Meter getNumberOfReceivedPdus() {
		return this.numberOfReceivedPdus;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!this.pduType.isInstance(e.getMessage())) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + this.pduType.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler AFTER any decoders but BEFORE any transport protocol converters?");
		}

		this.numberOfReceivedPdus.mark();

		super.messageReceived(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.numberOfReceivedPdus = this.metricsRegistry.newMeter(
		        numberOfReceivedPdusMetricName(channel), "pdu-received",
		        TimeUnit.SECONDS);

		super.channelOpen(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.metricsRegistry
		        .removeMetric(numberOfReceivedPdusMetricName(channel));

		super.channelClosed(ctx, e);
	}

	private MetricName numberOfReceivedPdusMetricName(final Channel channel) {
		return new MetricName(Channel.class, "received-pdus",
		        ChannelUtils.toString(channel));
	}
}
