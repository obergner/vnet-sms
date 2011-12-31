/**
 * 
 */
package vnet.sms.common.wme;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginRequest;

/**
 * @author obergner
 * 
 */
public class LoginRequestReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, LoginRequest> {

	public LoginRequestReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final LoginRequest loginRequest) {
		super(messageReference,
		        WindowedMessageEvent.Type.LOGIN_REQUEST_RECEIVED,
		        upstreamMessageEvent, loginRequest);
	}
}
