/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.MessageEventType;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;

/**
 * @author obergner
 * 
 */
public class SendPingRequestAckEvent<ID extends Serializable> extends
        DownstreamSendMessageAcknowledgementEvent<ID, PingRequest> {

	public static final <ID extends Serializable> SendPingRequestAckEvent<ID> ack(
	        final ReceivedPingRequestEvent<ID> pingRequestReceived) {
		notNull(pingRequestReceived,
		        "Argument 'pingRequestReceived' must not be null");
		return new SendPingRequestAckEvent<ID>(
		        pingRequestReceived.getMessageReference(),
		        pingRequestReceived.getChannel(),
		        pingRequestReceived.getMessage());
	}

	private SendPingRequestAckEvent(final ID messageReference,
	        final Channel channel, final PingRequest message) {
		super(messageReference,
		        MessageEventType.SEND_PING_REQUEST_ACK, channel,
		        message, Acknowledgement.ack());
	}
}
