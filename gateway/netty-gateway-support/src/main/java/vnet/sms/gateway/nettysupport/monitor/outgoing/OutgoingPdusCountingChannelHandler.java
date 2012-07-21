/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import vnet.sms.gateway.nettysupport.ChannelUtils;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class OutgoingPdusCountingChannelHandler<TP> extends
        SimpleChannelHandler {

	public static final String	  NAME	= "vnet.sms.gateway:outgoing-pdus-counting-handler";

	private final Class<TP>	      pduType;

	private final MetricsRegistry	metricsRegistry;

	private Meter	              numberOfSentPdus;

	public OutgoingPdusCountingChannelHandler(final Class<TP> pduType,
	        final MetricsRegistry metricsRegistry) {
		notNull(pduType, "Argument 'pduType' must not be null");
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.pduType = pduType;
		this.metricsRegistry = metricsRegistry;
	}

	/**
	 * @return the numberOfSentPdus
	 */
	public final Meter getNumberOfSentPdus() {
		return this.numberOfSentPdus;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!this.pduType.isInstance(e.getMessage())) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + this.pduType.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler AFTER any transport protocol converters but BEFORE any encoders?");
		}
		this.numberOfSentPdus.mark();
		super.writeRequested(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.numberOfSentPdus = this.metricsRegistry.newMeter(
		        numberOfSentPdusMetricName(channel), "pdu-sent",
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
		this.metricsRegistry.removeMetric(numberOfSentPdusMetricName(channel));

		super.channelClosed(ctx, e);
	}

	private MetricName numberOfSentPdusMetricName(final Channel channel) {
		return new MetricName(Channel.class, "sent-pdus",
		        ChannelUtils.toString(channel));
	}
}
