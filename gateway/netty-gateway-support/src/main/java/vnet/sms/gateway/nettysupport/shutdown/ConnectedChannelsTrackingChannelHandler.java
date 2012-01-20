/**
 * 
 */
package vnet.sms.gateway.nettysupport.shutdown;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
@ChannelHandler.Sharable
public class ConnectedChannelsTrackingChannelHandler extends
        SimpleChannelUpstreamHandler {

	public static final String	NAME	= "vnet.sms.gateway:connected-channels-tracking-handler";

	private final Logger	   log	 = LoggerFactory.getLogger(getClass());

	private final ChannelGroup	allConnectedChannels;

	public ConnectedChannelsTrackingChannelHandler(
	        final ChannelGroup allConnectedChannels) {
		notNull(allConnectedChannels,
		        "Argument 'allConnectedChannels' must not be null");
		this.allConnectedChannels = allConnectedChannels;
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.log.info(
		        "Channel [{}] has been connected and will be registered as a connected channel for later shutdown",
		        ctx.getChannel());
		this.allConnectedChannels.add(ctx.getChannel());
		super.channelConnected(ctx, e);
	}

	/**
	 * @return the allConnectedChannels
	 */
	public final ChannelGroup getAllConnectedChannels() {
		return this.allConnectedChannels;
	}
}
