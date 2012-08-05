/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class ReceivedPingRequestEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, PingRequest> {

	public ReceivedPingRequestEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final PingRequest pingRequest) {
		super(messageReference, MessageEventType.RECEIVED_PING_REQUEST,
		        upstreamMessageEvent, pingRequest);
	}
}
