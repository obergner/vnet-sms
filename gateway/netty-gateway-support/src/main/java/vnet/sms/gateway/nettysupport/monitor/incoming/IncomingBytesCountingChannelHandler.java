/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import vnet.sms.gateway.nettysupport.ChannelUtils;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class IncomingBytesCountingChannelHandler extends
        SimpleChannelUpstreamHandler {

	public static final String	  NAME	= "vnet.sms.gateway:incoming-bytes-counting-handler";

	private final MetricsRegistry	metricsRegistry;

	private Histogram	          numberOfReceivedBytes;

	private Counter	              totalNumberOfReceivedBytes;

	public IncomingBytesCountingChannelHandler(
	        final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof ChannelBuffer)) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + ChannelBuffer.class.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler BEFORE any decoders?");
		}

		final int readableBytes = ChannelBuffer.class.cast(e.getMessage())
		        .readableBytes();
		this.numberOfReceivedBytes.update(readableBytes);
		this.totalNumberOfReceivedBytes.inc(readableBytes);

		super.messageReceived(ctx, e);
	}

	/**
	 * @return the numberOfReceivedBytes
	 */
	public final Histogram getNumberOfReceivedBytes() {
		return this.numberOfReceivedBytes;
	}

	/**
	 * @return the totalNumberOfReceivedBytes
	 */
	public final Counter getTotalNumberOfReceivedBytes() {
		return this.totalNumberOfReceivedBytes;
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.numberOfReceivedBytes = this.metricsRegistry.newHistogram(
		        receivedBytesMetricName(channel), false);
		this.totalNumberOfReceivedBytes = this.metricsRegistry
		        .newCounter(totalReceivedBytesMetricName(channel));

		super.channelOpen(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.metricsRegistry.removeMetric(receivedBytesMetricName(ctx
		        .getChannel()));
		this.metricsRegistry.removeMetric(totalReceivedBytesMetricName(ctx
		        .getChannel()));

		super.channelClosed(ctx, e);
	}

	private MetricName totalReceivedBytesMetricName(final Channel channel) {
		return new MetricName(Channel.class, "total-received-bytes",
		        ChannelUtils.toString(channel));
	}

	private MetricName receivedBytesMetricName(final Channel channel) {
		return new MetricName(Channel.class, "received-bytes",
		        ChannelUtils.toString(channel));
	}
}
