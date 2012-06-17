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
public class PingRequestReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, PingRequest> {

	public PingRequestReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final PingRequest pingRequest) {
		super(messageReference, MessageEventType.PING_REQUEST_RECEIVED,
		        upstreamMessageEvent, pingRequest);
	}
}
