package vnet.sms.gateway.nettysupport.test;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.common.wme.send.SendSmsEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;

public class ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler
        extends
        TransportProtocolAdaptingDownstreamChannelHandler<Integer, Message> {

	@Override
	protected Message convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertLoginRequestAcceptedEventToPdu(
	        final ReceivedLoginRequestAckedEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertLoginRequestRejectedEventToPdu(
	        final ReceivedLoginRequestNackedEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<Integer, ?> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertSendSmsEventToPdu(final SendSmsEvent e) {
		return e.getMessage();
	}

	@Override
	protected Message convertReceivedSmsAckedEventToPdu(
	        final ReceivedSmsAckedEvent<Integer> e) {
		return e.getAcknowledgement();
	}

	@Override
	protected Message convertReceivedSmsNackedEventToPdu(
	        final ReceivedSmsNackedEvent<Integer> e) {
		return e.getAcknowledgement();
	}
}
