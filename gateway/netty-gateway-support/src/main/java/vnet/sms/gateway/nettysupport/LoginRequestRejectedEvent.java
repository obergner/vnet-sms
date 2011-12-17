/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;

/**
 * @author obergner
 * 
 */
public class LoginRequestRejectedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, LoginRequest> {

	public static final <I extends Serializable> LoginRequestRejectedEvent<I> reject(
	        final LoginRequestReceivedEvent<I> loginRequestReceivedEvent) {
		notNull(loginRequestReceivedEvent,
		        "Argument 'loginRequestReceivedEvent' must not be null");
		return new LoginRequestRejectedEvent<I>(
		        loginRequestReceivedEvent.getMessageReference(),
		        loginRequestReceivedEvent.getChannel(),
		        loginRequestReceivedEvent.getMessage());
	}

	private LoginRequestRejectedEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference, channel, message, channel.getRemoteAddress(),
		        Acknowledgement.nack());
	}
}
