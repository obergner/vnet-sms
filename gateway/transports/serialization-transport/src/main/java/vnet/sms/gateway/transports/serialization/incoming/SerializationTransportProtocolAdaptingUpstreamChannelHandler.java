/**
 * 
 */
package vnet.sms.gateway.transports.serialization.incoming;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

/**
 * @author obergner
 * 
 */
public class SerializationTransportProtocolAdaptingUpstreamChannelHandler
        extends
        TransportProtocolAdaptingUpstreamChannelHandler<Integer, ReferenceableMessageContainer> {

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#typeOf(java.lang.Object)
	 */
	@Override
	protected PduType typeOf(final Object pdu) {
		if (!(pdu instanceof ReferenceableMessageContainer)) {
			return TransportProtocolAdaptingUpstreamChannelHandler.PduType.UNKNOWN;
		}
		final GsmPdu payload = ((ReferenceableMessageContainer) pdu)
		        .getMessage();
		final TransportProtocolAdaptingUpstreamChannelHandler.PduType pduType;
		if (payload instanceof LoginRequest) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.LOGIN_REQUEST;
		} else if (payload instanceof LoginResponse) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.LOGIN_RESPONSE;
		} else if (payload instanceof PingRequest) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.PING_REQUEST;
		} else if (payload instanceof PingResponse) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.PING_RESPONSE;
		} else if (payload instanceof Sms) {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.SMS;
		} else {
			pduType = TransportProtocolAdaptingUpstreamChannelHandler.PduType.UNKNOWN;
		}
		return pduType;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#extractWindowId(java.lang.Object)
	 */
	@Override
	protected Integer extractWindowId(final ReferenceableMessageContainer pdu) {
		return pdu.getMessageReference();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#convertPduToLoginRequest(java.lang.Object)
	 */
	@Override
	protected LoginRequest convertPduToLoginRequest(
	        final ReferenceableMessageContainer loginRequestPdu) {
		return (LoginRequest) loginRequestPdu.getMessage();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#convertPduToLoginResponse(java.lang.Object)
	 */
	@Override
	protected LoginResponse convertPduToLoginResponse(
	        final ReferenceableMessageContainer loginResponsePdu) {
		return (LoginResponse) loginResponsePdu.getMessage();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#convertPduToPingRequest(java.lang.Object)
	 */
	@Override
	protected PingRequest convertPduToPingRequest(
	        final ReferenceableMessageContainer pingRequestPdu) {
		return (PingRequest) pingRequestPdu.getMessage();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#convertPduToPingResponse(java.lang.Object)
	 */
	@Override
	protected PingResponse convertPduToPingResponse(
	        final ReferenceableMessageContainer pingResponsePdu) {
		return (PingResponse) pingResponsePdu.getMessage();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler#convertPduToSms(java.lang.Object)
	 */
	@Override
	protected Sms convertPduToSms(final ReferenceableMessageContainer smsPdu) {
		return (Sms) smsPdu.getMessage();
	}
}
