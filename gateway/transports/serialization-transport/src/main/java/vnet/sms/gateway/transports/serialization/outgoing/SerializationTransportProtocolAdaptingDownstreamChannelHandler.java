package vnet.sms.gateway.transports.serialization.outgoing;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.acknowledge.SendLoginRequestAckEvent;
import vnet.sms.common.wme.acknowledge.SendLoginRequestNackEvent;
import vnet.sms.common.wme.acknowledge.SendSmsAckEvent;
import vnet.sms.common.wme.acknowledge.SendSmsNackEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.common.wme.send.SendSmsEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class SerializationTransportProtocolAdaptingDownstreamChannelHandler
        extends
        TransportProtocolAdaptingDownstreamChannelHandler<Integer, ReferenceableMessageContainer> {

	@Override
	protected ReferenceableMessageContainer convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(e.getMessageReference(),
		        e.getMessage());
	}

	@Override
	protected ReferenceableMessageContainer convertLoginRequestAcceptedEventToPdu(
	        final SendLoginRequestAckEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(),
		        LoginResponse.accept(e.getMessage()));
	}

	@Override
	protected ReferenceableMessageContainer convertLoginRequestRejectedEventToPdu(
	        final SendLoginRequestNackEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(),
		        LoginResponse.reject(e.getMessage()));
	}

	@Override
	protected ReferenceableMessageContainer convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<Integer, ?> e) {
		final GsmPdu rejectedMessage = e.getMessage();
		final GsmPdu nack;
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

	@Override
	protected ReferenceableMessageContainer convertSendSmsEventToPdu(
	        final SendSmsEvent e) {
		// FIXME: Replace message reference with proper value as soon as we
		// support outgoing windowing
		return ReferenceableMessageContainer.wrap(Integer.MIN_VALUE,
		        e.getMessage());
	}

	@Override
	protected ReferenceableMessageContainer convertReceivedSmsAckedEventToPdu(
	        final SendSmsAckEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(), Acknowledgement.ack());
	}

	@Override
	protected ReferenceableMessageContainer convertReceivedSmsNackedEventToPdu(
	        final SendSmsNackEvent<Integer> e) {
		return ReferenceableMessageContainer.wrap(
		        e.getAcknowledgedMessageReference(), Acknowledgement.nack());
	}
}
