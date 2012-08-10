/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class ReceivedLoginRequestAcknowledgementEvent<ID extends Serializable>
        extends UpstreamWindowedMessageEvent<ID, LoginResponse> {

	public ReceivedLoginRequestAcknowledgementEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final LoginResponse loginResponse) {
		super(messageReference,
		        MessageEventType.RECEIVED_LOGIN_REQUEST_ACKNOWLEDGEMENT,
		        upstreamMessageEvent, loginResponse);
	}
}
