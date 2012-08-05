/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.wme.MessageEventType;
import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;

/**
 * @author obergner
 * 
 */
public class SendLoginRequestNackEvent<ID extends Serializable> extends
        DownstreamSendMessageAcknowledgementEvent<ID, LoginRequest> {

	public static final <I extends Serializable> SendLoginRequestNackEvent<I> reject(
	        final ReceivedLoginRequestEvent<I> loginRequestReceivedEvent) {
		notNull(loginRequestReceivedEvent,
		        "Argument 'loginRequestReceivedEvent' must not be null");
		return new SendLoginRequestNackEvent<I>(
		        loginRequestReceivedEvent.getMessageReference(),
		        loginRequestReceivedEvent.getChannel(),
		        loginRequestReceivedEvent.getMessage());
	}

	private SendLoginRequestNackEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference, MessageEventType.SEND_LOGIN_REQUEST_NACK,
		        channel, message, Acknowledgement.nack());
	}
}
