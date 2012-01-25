/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.MessageType;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;

/**
 * @author obergner
 * 
 */
public class ReceivedPingRequestAckedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAcknowledgedEvent<ID, PingRequest> {

	public static final <ID extends Serializable> ReceivedPingRequestAckedEvent<ID> ack(
	        final PingRequestReceivedEvent<ID> pingRequestReceived) {
		notNull(pingRequestReceived,
		        "Argument 'pingRequestReceived' must not be null");
		return new ReceivedPingRequestAckedEvent<ID>(
		        pingRequestReceived.getMessageReference(),
		        pingRequestReceived.getChannel(),
		        pingRequestReceived.getMessage());
	}

	private ReceivedPingRequestAckedEvent(final ID messageReference,
	        final Channel channel, final PingRequest message) {
		super(messageReference, MessageType.RECEIVED_PING_REQUEST_ACKNOWLEDGED,
		        channel, message, Acknowledgement.ack());
	}
}
