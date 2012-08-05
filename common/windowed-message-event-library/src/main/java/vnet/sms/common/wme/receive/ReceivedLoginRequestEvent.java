/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class ReceivedLoginRequestEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, LoginRequest> {

	public ReceivedLoginRequestEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final LoginRequest loginRequest) {
		super(messageReference, MessageEventType.RECEIVED_LOGIN_REQUEST,
		        upstreamMessageEvent, loginRequest);
	}
}
