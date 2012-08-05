/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.common.wme.acknowledge.SendLoginRequestAckEvent;
import vnet.sms.common.wme.acknowledge.SendLoginRequestNackEvent;
import vnet.sms.common.wme.acknowledge.SendSmsAckEvent;
import vnet.sms.common.wme.acknowledge.SendSmsNackEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.common.wme.send.SendSmsEvent;
import vnet.sms.gateway.nettysupport.WindowedChannelHandler;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;

/**
 * @author obergner
 * 
 */
public abstract class TransportProtocolAdaptingDownstreamChannelHandler<ID extends Serializable, TP>
        extends WindowedChannelHandler<ID> {

	public static final String	NAME	= "vnet.sms.gateway:outgoing-transport-protocol-adapter-handler";

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) {
		final TP pdu = convertSendPingRequestEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final SendLoginRequestAckEvent<ID> e) {
		final TP pdu = convertLoginRequestAcceptedEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final SendLoginRequestNackEvent<ID> e) {
		final TP pdu = convertLoginRequestRejectedEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
	        final ChannelHandlerContext ctx,
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e) {
		final TP pdu = convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeSmsRequested(final ChannelHandlerContext ctx,
	        final SendSmsEvent e) throws Exception {
		final TP pdu = convertSendSmsEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeReceivedSmsAckedRequested(
	        final ChannelHandlerContext ctx, final SendSmsAckEvent<ID> e)
	        throws Exception {
		final TP pdu = convertReceivedSmsAckedEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeReceivedSmsNackedRequested(
	        final ChannelHandlerContext ctx, final SendSmsNackEvent<ID> e)
	        throws Exception {
		final TP pdu = convertReceivedSmsNackedEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	protected abstract TP convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<ID> e);

	protected abstract TP convertLoginRequestAcceptedEventToPdu(
	        final SendLoginRequestAckEvent<ID> e);

	protected abstract TP convertLoginRequestRejectedEventToPdu(
	        final SendLoginRequestNackEvent<ID> e);

	protected abstract TP convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e);

	protected abstract TP convertSendSmsEventToPdu(final SendSmsEvent e);

	protected abstract TP convertReceivedSmsAckedEventToPdu(
	        final SendSmsAckEvent<ID> e);

	protected abstract TP convertReceivedSmsNackedEventToPdu(
	        final SendSmsNackEvent<ID> e);
}
