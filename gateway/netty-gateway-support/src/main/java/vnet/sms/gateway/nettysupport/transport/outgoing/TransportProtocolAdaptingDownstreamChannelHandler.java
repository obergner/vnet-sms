/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
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
	        final ReceivedLoginRequestAckedEvent<ID> e) {
		final TP pdu = convertLoginRequestAcceptedEventToPdu(e);
		getLog().trace("{} converted to {}", e, pdu);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestNackedEvent<ID> e) {
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

	protected abstract TP convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<ID> e);

	protected abstract TP convertLoginRequestAcceptedEventToPdu(
	        final ReceivedLoginRequestAckedEvent<ID> e);

	protected abstract TP convertLoginRequestRejectedEventToPdu(
	        final ReceivedLoginRequestNackedEvent<ID> e);

	protected abstract TP convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e);
}
