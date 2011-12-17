/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class EchoTestClientResponseHandler extends SimpleChannelUpstreamHandler {

	private final Logger	       log	= LoggerFactory.getLogger(getClass());

	private final ResponseListener	responseListener;

	public EchoTestClientResponseHandler(final ResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		this.log.debug("Received message event [{}]", e);
		final String response = (String) e.getMessage();
		this.log.debug("Received telnet response [{}]", response);

		this.responseListener.responseReceived(response);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
	        final ExceptionEvent e) throws Exception {
		this.log.error("Error processing response: "
		        + e.getCause().getMessage());
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.log.debug("Disconnected from channel [{}]", e.getChannel());
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void channelClosed(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.log.debug("Channel [{}] closed", e.getChannel());
		super.channelClosed(ctx, e);
	}

}
