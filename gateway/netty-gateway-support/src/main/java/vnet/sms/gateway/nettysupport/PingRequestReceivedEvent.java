/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.PingRequest;

/**
 * @author obergner
 * 
 */
public class PingRequestReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, PingRequest> {

	public PingRequestReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final PingRequest pingRequest) {
		super(messageReference, upstreamMessageEvent, pingRequest);
	}
}
