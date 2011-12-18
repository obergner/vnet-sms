/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;
import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import vnet.sms.common.messages.PingRequest;

/**
 * @author obergner
 * 
 */
public final class SendPingRequestEvent<ID extends Serializable> extends
        DownstreamWindowedMessageEvent<ID, PingRequest> {

	public SendPingRequestEvent(final ID messageReference,
	        final Channel channel, final PingRequest message,
	        final SocketAddress remoteAddress) {
		super(messageReference, channel, Channels.future(channel, false),
		        message, remoteAddress);
	}
}
