package vnet.sms.gateway.nettysupport.test;

import java.util.concurrent.atomic.AtomicInteger;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;

public class ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler
        extends
        TransportProtocolAdaptingUpstreamChannelHandler<Integer, GsmPdu> {

	private final AtomicInteger	nextWindowId	= new AtomicInteger(1);

	@Override
	protected Integer extractWindowId(final GsmPdu pdu) {
		return this.nextWindowId.getAndIncrement();
	}

	@Override
	protected TransportProtocolAdaptingUpstreamChannelHandler.PduType typeOf(
	        final Object pdu) {
		final TransportProtocolAdaptingUpstreamChannelHandler.PduType pduType;
		if (pdu instanceof LoginRequest) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.LOGIN_REQUEST;
		} else if (pdu instanceof LoginResponse) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.LOGIN_RESPONSE;
		} else if (pdu instanceof PingRequest) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.PING_REQUEST;
		} else if (pdu instanceof PingResponse) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.PING_RESPONSE;
		} else if (pdu instanceof Sms) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.SMS;
		} else {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.UNKNOWN;
		}
		return pduType;
	}

	@Override
	protected LoginRequest convertPduToLoginRequest(final GsmPdu loginRequestPdu) {
		return (LoginRequest) loginRequestPdu;
	}

	@Override
	protected LoginResponse convertPduToLoginResponse(
	        final GsmPdu loginResponsePdu) {
		return (LoginResponse) loginResponsePdu;
	}

	@Override
	protected PingRequest convertPduToPingRequest(final GsmPdu pingRequestPdu) {
		return (PingRequest) pingRequestPdu;
	}

	@Override
	protected PingResponse convertPduToPingResponse(final GsmPdu pingResponsePdu) {
		return (PingResponse) pingResponsePdu;
	}

	@Override
	protected Sms convertPduToSms(final GsmPdu smsPdu) {
		return (Sms) smsPdu;
	}
}
