/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class ReceivedPingRequestAcknowledgementEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, PingResponse> {

	public ReceivedPingRequestAcknowledgementEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final PingResponse pingResponse) {
		super(messageReference, MessageEventType.RECEIVED_PING_REQUEST_ACKNOWLEDGEMENT,
		        upstreamMessageEvent, pingResponse);
	}
}
