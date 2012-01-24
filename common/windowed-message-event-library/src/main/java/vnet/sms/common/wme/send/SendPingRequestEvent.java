/**
 * 
 */
package vnet.sms.common.wme.send;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public final class SendPingRequestEvent<ID extends Serializable> extends
        DownstreamWindowedMessageEvent<ID, PingRequest> {

	public SendPingRequestEvent(final ID messageReference,
	        final Channel channel, final PingRequest message) {
		super(messageReference, MessageType.SEND_PING_REQUEST, channel,
		        Channels.future(channel, false), message);
	}
}
