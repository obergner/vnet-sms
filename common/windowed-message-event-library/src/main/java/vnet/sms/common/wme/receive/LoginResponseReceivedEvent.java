/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.MessageEventType;

/**
 * @author obergner
 * 
 */
public class LoginResponseReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, LoginResponse> {

	public LoginResponseReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final LoginResponse loginResponse) {
		super(messageReference, MessageEventType.LOGIN_RESPONSE_RECEIVED,
		        upstreamMessageEvent, loginResponse);
	}
}
