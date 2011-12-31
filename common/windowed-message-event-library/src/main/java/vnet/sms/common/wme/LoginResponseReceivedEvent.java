/**
 * 
 */
package vnet.sms.common.wme;

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
		super(messageReference,
		        WindowedMessageEvent.Type.LOGIN_RESPONSE_RECEIVED,
		        upstreamMessageEvent, loginResponse);
	}
}
