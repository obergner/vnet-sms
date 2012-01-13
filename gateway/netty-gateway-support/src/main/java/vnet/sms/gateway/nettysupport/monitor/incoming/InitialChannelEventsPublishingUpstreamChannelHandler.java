/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * @author obergner
 * 
 */
public class InitialChannelEventsPublishingUpstreamChannelHandler extends
        SimpleChannelUpstreamHandler {

	private final InitialChannelEventsMonitor	initialChannelEventsMonitor;

	/**
	 * @param initialChannelEventsMonitor
	 */
	public InitialChannelEventsPublishingUpstreamChannelHandler(
	        final InitialChannelEventsMonitor initialChannelEventsMonitor) {
		notNull(initialChannelEventsMonitor,
		        "Argument 'initialChannelEventsMonitor' must not be null");
		this.initialChannelEventsMonitor = initialChannelEventsMonitor;
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelOpen(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelOpen(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.initialChannelEventsMonitor.channelOpened(e.getChannel());
		super.channelOpen(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelBound(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelBound(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.initialChannelEventsMonitor.channelBound(e.getChannel(),
		        (SocketAddress) e.getValue());
		super.channelBound(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.initialChannelEventsMonitor.channelConnected(e.getChannel(),
		        (SocketAddress) e.getValue());
		super.channelConnected(ctx, e);
	}
}
