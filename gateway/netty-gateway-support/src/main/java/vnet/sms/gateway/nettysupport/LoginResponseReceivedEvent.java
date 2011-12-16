/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginResponse;

/**
 * @author obergner
 * 
 */
public class LoginResponseReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, LoginResponse> {

	public LoginResponseReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final LoginResponse loginResponse) {
		super(messageReference, upstreamMessageEvent, loginResponse);
	}
}
