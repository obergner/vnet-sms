/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.gateway.nettysupport.DownstreamWindowedChannelHandler;
import vnet.sms.gateway.nettysupport.LoginRequestAcceptedEvent;
import vnet.sms.gateway.nettysupport.LoginRequestRejectedEvent;
import vnet.sms.gateway.nettysupport.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;

/**
 * @author obergner
 * 
 */
public abstract class TransportProtocolAdaptingDownstreamChannelHandler<ID extends Serializable, TP>
        extends DownstreamWindowedChannelHandler<ID> {

	public static final String	NAME	= "vnet.sms.gateway:outgoing-transport-protocol-adapter-handler";

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) {
		final TP pdu = convertSendPingRequestEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestAcceptedEvent<ID> e) {
		final TP pdu = convertLoginRequestAcceptedEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestRejectedEvent<ID> e) {
		final TP pdu = convertLoginRequestRejectedEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
	        final ChannelHandlerContext ctx,
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e) {
		final TP pdu = convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	protected abstract TP convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<ID> e);

	protected abstract TP convertLoginRequestAcceptedEventToPdu(
	        final LoginRequestAcceptedEvent<ID> e);

	protected abstract TP convertLoginRequestRejectedEventToPdu(
	        final LoginRequestRejectedEvent<ID> e);

	protected abstract TP convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e);
}
