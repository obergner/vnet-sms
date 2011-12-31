/**
 * 
 */
package vnet.sms.common.wme;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;

/**
 * @author obergner
 * 
 */
public class LoginRequestAcceptedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, LoginRequest> {

	public static final <I extends Serializable> LoginRequestAcceptedEvent<I> accept(
	        final LoginRequestReceivedEvent<I> loginRequestReceivedEvent) {
		notNull(loginRequestReceivedEvent,
		        "Argument 'loginRequestReceivedEvent' must not be null");
		return new LoginRequestAcceptedEvent<I>(
		        loginRequestReceivedEvent.getMessageReference(),
		        loginRequestReceivedEvent.getChannel(),
		        loginRequestReceivedEvent.getMessage());
	}

	private LoginRequestAcceptedEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference,
		        WindowedMessageEvent.Type.LOGIN_REQUEST_ACCEPTED, channel,
		        message, Acknowledgement.ack());
	}
}
