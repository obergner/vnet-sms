/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.gateway.nettysupport.LoginRequestAcceptedEvent;
import vnet.sms.gateway.nettysupport.LoginRequestRejectedEvent;
import vnet.sms.gateway.nettysupport.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.WindowedChannelHandler;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

/**
 * @author obergner
 * 
 */
public abstract class TransportProtocolAdaptingDownstreamChannelHandler<ID extends Serializable, TP>
        extends WindowedChannelHandler<ID> {

	public static final String	                           NAME	            = "vnet.sms.gateway:outgoing-transport-protocol-adapter-handler";

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	public TransportProtocolAdaptingDownstreamChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		this.monitorRegistry = monitorRegistry;
	}

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) {
		final TP pdu = convertSendPingRequestEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
		getMonitorCallback().sendPingRequest();
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestAcceptedEvent<ID> e) {
		final TP pdu = convertLoginRequestAcceptedEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
		getMonitorCallback().sendLoginRequestAccepted();
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestRejectedEvent<ID> e) {
		final TP pdu = convertLoginRequestRejectedEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
		getMonitorCallback().sendLoginRequestRejected();
	}

	@Override
	protected void writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
	        final ChannelHandlerContext ctx,
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e) {
		final TP pdu = convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(e);
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e
		        .getFuture(), pdu, e.getRemoteAddress()));
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

	protected abstract TP convertSendPingRequestEventToPdu(
	        final SendPingRequestEvent<ID> e);

	protected abstract TP convertLoginRequestAcceptedEventToPdu(
	        final LoginRequestAcceptedEvent<ID> e);

	protected abstract TP convertLoginRequestRejectedEventToPdu(
	        final LoginRequestRejectedEvent<ID> e);

	protected abstract TP convertNonLoginMessageReceivedOnUnauthenticatedChannelEventToPdu(
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e);
}
