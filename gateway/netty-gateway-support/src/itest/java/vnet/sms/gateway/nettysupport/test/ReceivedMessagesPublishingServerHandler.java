/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
class ReceivedMessagesPublishingServerHandler extends
        SimpleChannelUpstreamHandler {

	private final Logger	                    log	      = LoggerFactory
	                                                              .getLogger(getClass());

	private final Set<ReceivedMessagesListener>	listeners	= new CopyOnWriteArraySet<ReceivedMessagesListener>();

	public void addListener(final ReceivedMessagesListener listener) {
		this.listeners.add(listener);
	}

	void clear() {
		this.listeners.clear();
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		final Object message = e.getMessage();
		this.log.debug("Received message [{}]", message);
		for (final ReceivedMessagesListener listener : this.listeners) {
			listener.messageReceived(message);
		}
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
	        final ExceptionEvent e) throws Exception {
		this.log.error("Caught exception from downstream: " + e.getCause(),
		        e.getCause());
		e.getChannel().close();
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		clear();
		super.channelDisconnected(ctx, e);
	}
}
