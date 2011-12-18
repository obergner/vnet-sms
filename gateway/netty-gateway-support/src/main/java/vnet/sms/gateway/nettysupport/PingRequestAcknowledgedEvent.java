/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.PingRequest;

/**
 * @author obergner
 * 
 */
public class PingRequestAcknowledgedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, PingRequest> {

	public static final <ID extends Serializable> PingRequestAcknowledgedEvent<ID> acknowledge(
	        final PingRequestReceivedEvent<ID> pingRequestReceived) {
		notNull(pingRequestReceived,
		        "Argument 'pingRequestReceived' must not be null");
		return new PingRequestAcknowledgedEvent<ID>(
		        pingRequestReceived.getMessageReference(),
		        pingRequestReceived.getChannel(),
		        pingRequestReceived.getMessage(),
		        pingRequestReceived.getRemoteAddress());
	}

	private PingRequestAcknowledgedEvent(final ID messageReference,
	        final Channel channel, final PingRequest message,
	        final SocketAddress remoteAddress) {
		super(messageReference, channel, message, remoteAddress,
		        Acknowledgement.ack());
	}
}
