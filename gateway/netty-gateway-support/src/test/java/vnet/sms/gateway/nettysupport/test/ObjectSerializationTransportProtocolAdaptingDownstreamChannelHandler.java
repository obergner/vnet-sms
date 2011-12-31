package vnet.sms.gateway.nettysupport.test;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.monitor.TestChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;

public class ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler
        extends
        TransportProtocolAdaptingDownstreamChannelHandler<Integer, Message> {

	public ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler() {
		this(new TestChannelMonitorRegistry());
	}

	public ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler(
	        final ChannelMonitor.Callback channelMetricsListener) {
		this(new TestChannelMonitorRegistry(channelMetricsListener));
	}

	public ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler(
	        final ChannelMonitorRegistry channelMetricsRegistry) {
		super(channelMetricsRegistry);
	}

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
