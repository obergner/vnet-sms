/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.PingResponse;

/**
 * @author obergner
 * 
 */
public class PingResponseReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, PingResponse> {

	public PingResponseReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final PingResponse pingResponse) {
		super(messageReference, upstreamMessageEvent, pingResponse);
	}
}
