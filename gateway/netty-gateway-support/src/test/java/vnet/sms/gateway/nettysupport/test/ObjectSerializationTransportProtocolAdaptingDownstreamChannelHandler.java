package vnet.sms.gateway.nettysupport.test;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.LoginRequestAcceptedEvent;
import vnet.sms.gateway.nettysupport.LoginRequestRejectedEvent;
import vnet.sms.gateway.nettysupport.SendPingRequestEvent;
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
	        final LoginRequestAcceptedEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertLoginRequestRejectedEventToPdu(
	        final LoginRequestRejectedEvent<Integer> e) {
		return e.getMessage();
	}

	@Override
	protected Message convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<Integer, ?> e) {
		return e.getMessage();
	}
}
