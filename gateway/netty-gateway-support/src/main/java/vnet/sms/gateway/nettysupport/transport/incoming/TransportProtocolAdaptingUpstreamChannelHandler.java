/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
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
import vnet.sms.gateway.nettysupport.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

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

	private final Logger	                               log	            = LoggerFactory
	                                                                                .getLogger(getClass());

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	public TransportProtocolAdaptingUpstreamChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		this.monitorRegistry = monitorRegistry;
	}

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
			getMonitorCallback().loginRequestReceived();
			break;
		case LOGIN_RESPONSE:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToLoginResponse((TP) pdu);
			getMonitorCallback().loginResponseReceived();
			break;
		case PING_REQUEST:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToPingRequest((TP) pdu);
			getMonitorCallback().pingRequestReceived();
			break;
		case PING_RESPONSE:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToPingResponse((TP) pdu);
			getMonitorCallback().pingResponseReceived();
			break;
		case SMS:
			extractedWindowId = extractWindowId((TP) pdu);
			convertedPdu = convertPduToSms((TP) pdu);
			getMonitorCallback().smsReceived();
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

	private ChannelMonitor.Callback getMonitorCallback() {
		return this.monitorCallback.get();
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		if (!this.monitorCallback.compareAndSet(ChannelMonitor.Callback.NULL,
		        this.monitorRegistry.registerChannel(ctx.getChannel()))) {
			throw new IllegalStateException(
			        "Cannot register a ChannelMonitorCallback for this ChannelHandler more than once");
		}
		super.channelConnected(ctx, e);
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
