package vnet.sms.gateway.nettysupport.test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.acknowledge.SendLoginRequestAckEvent;
import vnet.sms.common.wme.acknowledge.SendLoginRequestNackEvent;
import vnet.sms.common.wme.acknowledge.SendSmsAckEvent;
import vnet.sms.common.wme.acknowledge.SendSmsNackEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.common.wme.send.SendSmsEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;

public class ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler
        extends
        TransportProtocolAdaptingDownstreamChannelHandler<Integer, GsmPdu> {

	@Override
	protected GsmPdu convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected GsmPdu convertLoginRequestAcceptedEventToPdu(
	        final SendLoginRequestAckEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected GsmPdu convertLoginRequestRejectedEventToPdu(
	        final SendLoginRequestNackEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected GsmPdu convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<Integer, ?> e) {
		return e.getMessage();
	}

	@Override
	protected GsmPdu convertSendSmsEventToPdu(final SendSmsEvent e) {
		return e.getMessage();
	}

	@Override
	protected GsmPdu convertReceivedSmsAckedEventToPdu(
	        final SendSmsAckEvent<Integer> e) {
		return e.getAcknowledgement();
	}

	@Override
	protected GsmPdu convertReceivedSmsNackedEventToPdu(
	        final SendSmsNackEvent<Integer> e) {
		return e.getAcknowledgement();
	}
}
