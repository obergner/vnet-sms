/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.isTrue;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.acknowledge.DownstreamReceivedMessageAcknowledgedEvent;

/**
 * @author obergner
 * 
 */
public final class NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID extends Serializable, M extends Message>
        extends DownstreamReceivedMessageAcknowledgedEvent<ID, M> {

	public static final <ID extends Serializable, M extends Message> NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, M> discardNonLoginMessage(
	        final WindowedMessageEvent<ID, M> nonLoginRequest) {
		isTrue(!(nonLoginRequest.getMessage() instanceof LoginRequest),
		        "Argument 'nonLoginRequest' must not be a LoginRequest");
		return new NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, M>(
		        nonLoginRequest.getMessageReference(),
		        nonLoginRequest.getChannel(), nonLoginRequest.getMessage());
	}

	private NonLoginMessageReceivedOnUnauthenticatedChannelEvent(
	        final ID messageReference, final Channel channel, final M message) {
		super(
		        messageReference,
		        MessageEventType.NON_LOGIN_MESSAGE_RECEIVED_ON_UNAUTHENTICATED_CHANNEL,
		        channel, message, Acknowledgement.nack());
	}
}
