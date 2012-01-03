/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.incoming;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public abstract class TransportProtocolAdaptingUpstreamChannelHandler<ID extends Serializable, TP>
        extends SimpleChannelUpstreamHandler {

	public static final String	NAME	= "vnet.sms.gateway:incoming-transport-protocol-adapter-handler";

	public enum PduType {

		UNKNOWN,

		LOGIN_REQUEST,

		LOGIN_RESPONSE,

		PING_REQUEST,

		PING_RESPONSE,

		SMS;
	}

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		final Object pdu = e.getMessage();
		getLog().trace("Attempting to convert PDU {} to message ...", pdu);
		final ID extractedWindowId;
		final Message convertedPdu;
		switch (typeOf(pdu)) {
		case LOGIN_REQUEST:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToLoginRequest((TP) pdu);
			break;
		case LOGIN_RESPONSE:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToLoginResponse((TP) pdu);
			break;
		case PING_REQUEST:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToPingRequest((TP) pdu);
			break;
		case PING_RESPONSE:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToPingResponse((TP) pdu);
			break;
		case SMS:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToSms((TP) pdu);
			break;
		case UNKNOWN:
		default:
			throw new IllegalStateException("Unsupported message type: "
			        + pdu.getClass());
		}
		getLog().trace("PDU {} converted to {}", pdu, convertedPdu);

		final WindowedMessageEvent<ID, ? extends Message> windowedMessageEvent = UpstreamMessageEventToWindowedMessageEventConverter.INSTANCE
		        .convert(extractedWindowId, (UpstreamMessageEvent) e,
		                convertedPdu);
		ctx.sendUpstream(windowedMessageEvent);
	}

	protected abstract PduType typeOf(final Object pdu);

	protected abstract ID extractWindowId(final TP pdu);

	protected abstract LoginRequest convertPduToLoginRequest(
	        final TP loginRequestPdu);

	protected abstract LoginResponse convertPduToLoginResponse(
	        final TP loginResponsePdu);

	protected abstract PingRequest convertPduToPingRequest(
	        final TP pingRequestPdu);

	protected abstract PingResponse convertPduToPingResponse(
	        final TP pingResponsePdu);

	protected abstract Sms convertPduToSms(final TP smsPdu);

	protected Logger getLog() {
		return this.log;
	}
}
