/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import vnet.sms.gateway.nettysupport.ChannelUtils;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class OutgoingBytesCountingChannelHandler extends SimpleChannelHandler {

	public static final String	  NAME	= "vnet.sms.gateway:outgoing-bytes-counting-handler";

	private final MetricsRegistry	metricsRegistry;

	private Histogram	          numberOfSentBytes;

	private Counter	              totalNumberOfSentBytes;

	public OutgoingBytesCountingChannelHandler(
	        final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	/**
	 * @return the numberOfSentBytes
	 */
	public final Histogram getNumberOfSentBytes() {
		return this.numberOfSentBytes;
	}

	/**
	 * @return the totalNumberOfSentBytes
	 */
	public final Counter getTotalNumberOfSentBytes() {
		return this.totalNumberOfSentBytes;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof ChannelBuffer)) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + ChannelBuffer.class.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler BEFORE any decoders?");
		}

		final ChannelBuffer bytes = ChannelBuffer.class.cast(e.getMessage());
		this.numberOfSentBytes.update(bytes.readableBytes());
		this.totalNumberOfSentBytes.inc(bytes.readableBytes());
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
		this.numberOfSentBytes = this.metricsRegistry.newHistogram(
		        numberOfSentBytesMetricName(channel), false);
		this.totalNumberOfSentBytes = this.metricsRegistry
		        .newCounter(totalNumberOfSentBytesMetricName(channel));

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
		this.metricsRegistry.removeMetric(numberOfSentBytesMetricName(channel));
		this.metricsRegistry
		        .removeMetric(totalNumberOfSentBytesMetricName(channel));

		super.channelClosed(ctx, e);
	}

	private MetricName totalNumberOfSentBytesMetricName(final Channel channel) {
		return new MetricName(Channel.class, "total-sent-bytes",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfSentBytesMetricName(final Channel channel) {
		return new MetricName(Channel.class, "sent-bytes",
		        ChannelUtils.toString(channel));
	}
}
