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
public class ReceivedLoginRequestNackedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, LoginRequest> {

	public static final <I extends Serializable> ReceivedLoginRequestNackedEvent<I> reject(
	        final LoginRequestReceivedEvent<I> loginRequestReceivedEvent) {
		notNull(loginRequestReceivedEvent,
		        "Argument 'loginRequestReceivedEvent' must not be null");
		return new ReceivedLoginRequestNackedEvent<I>(
		        loginRequestReceivedEvent.getAcknowledgedMessageReference(),
		        loginRequestReceivedEvent.getChannel(),
		        loginRequestReceivedEvent.getMessage());
	}

	private ReceivedLoginRequestNackedEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference, MessageType.RECEIVED_LOGIN_REQUEST_NACKED,
		        channel, message, Acknowledgement.nack());
	}
}
