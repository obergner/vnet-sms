/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.wme.MessageType;
import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;

/**
 * @author obergner
 * 
 */
public class ReceivedLoginRequestAckedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAcknowledgedEvent<ID, LoginRequest> {

	public static final <I extends Serializable> ReceivedLoginRequestAckedEvent<I> accept(
	        final LoginRequestReceivedEvent<I> loginRequestReceivedEvent) {
		notNull(loginRequestReceivedEvent,
		        "Argument 'loginRequestReceivedEvent' must not be null");
		return new ReceivedLoginRequestAckedEvent<I>(
		        loginRequestReceivedEvent.getMessageReference(),
		        loginRequestReceivedEvent.getChannel(),
		        loginRequestReceivedEvent.getMessage());
	}

	private ReceivedLoginRequestAckedEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference, MessageType.RECEIVED_LOGIN_REQUEST_ACKED,
		        channel, message, Acknowledgement.ack());
	}
}
