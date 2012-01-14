/**
 * 
 */
package vnet.sms.gateway.nettysupport.logging.incoming;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.MDC;

/**
 * @author obergner
 * 
 */
public class ChannelContextLoggingUpstreamChannelHandler extends
        SimpleChannelUpstreamHandler {

	public static final String	NAME	                = "vnet.sms.gateway:channel-context-logging-handler";

	public static final String	CURRENT_CHANNEL_MDC_KEY	= "currentChannel";

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		try {
			MDC.put(CURRENT_CHANNEL_MDC_KEY, e.getChannel().toString());
			super.messageReceived(ctx, e);
		} finally {
			MDC.remove(CURRENT_CHANNEL_MDC_KEY);
		}
	}
}
