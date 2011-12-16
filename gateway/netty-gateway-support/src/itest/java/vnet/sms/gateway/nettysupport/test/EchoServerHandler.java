/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class EchoServerHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ChannelGroup allChannels;

	public EchoServerHandler(final ChannelGroup allChannels) {
		this.allChannels = allChannels;
	}

	@Override
	public void channelOpen(final ChannelHandlerContext ctx,
			final ChannelStateEvent e) throws Exception {
		this.allChannels.add(e.getChannel());
		ctx.sendUpstream(e);
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final MessageEvent e) throws Exception {
		final Object message = e.getMessage();
		this.log.debug("Received echo message [{}]", message);
		final ChannelFuture messageHasBeenEchoed = Channels.write(
				e.getChannel(), message);
		messageHasBeenEchoed.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future)
					throws Exception {
				if (!future.isSuccess()) {
					EchoServerHandler.this.log.error("Failed to echo message ["
							+ message + "]: " + future.getCause().getMessage(),
							future.getCause());
					future.getChannel().close();
				} else {
					EchoServerHandler.this.log.debug("Echoed message [{}]",
							message);
				}
			}
		});
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final ExceptionEvent e) throws Exception {
		this.log.error("Caught exception from downstream: " + e.getCause(),
				e.getCause());
		e.getChannel().close();
	}
}
