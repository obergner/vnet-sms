package vnet.sms.gateway.transports.serialization.outgoing;

import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class SerializationTransportProtocolAdaptingDownstreamChannelHandler
        extends
        TransportProtocolAdaptingDownstreamChannelHandler<Integer, ReferenceableMessageContainer> {

	@Override
	protected ReferenceableMessageContainer convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(), e.getMessage());
	}

	@Override
	protected ReferenceableMessageContainer convertLoginRequestAcceptedEventToPdu(
	        final ReceivedLoginRequestAckedEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(),
		        LoginResponse.accept(e.getMessage()));
	}

	@Override
	protected ReferenceableMessageContainer convertLoginRequestRejectedEventToPdu(
	        final ReceivedLoginRequestNackedEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(),
		        LoginResponse.reject(e.getMessage()));
	}

	@Override
	protected ReferenceableMessageContainer convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<Integer, ?> e) {
		final Message rejectedMessage = e.getMessage();
		final Message nack;
		if (rejectedMessage instanceof PingRequest) {
			final PingRequest rejectedPing = PingRequest.class
			        .cast(rejectedMessage);
			nack = PingResponse.reject(rejectedPing);
		} else {
			throw new IllegalStateException(
			        "Currently, we only support rejecting PingRequests as non-login messages");
		}
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(), nack);
	}
}
