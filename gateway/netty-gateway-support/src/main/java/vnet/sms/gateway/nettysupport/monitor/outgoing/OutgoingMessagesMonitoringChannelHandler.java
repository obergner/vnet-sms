/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.WindowedChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends WindowedChannelHandler<ID> {

	public static final String	                           NAME	            = "vnet.sms.gateway:outgoing-messages-monitoring-handler";

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	public OutgoingMessagesMonitoringChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		this.monitorRegistry = monitorRegistry;
	}

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) {
		getMonitorCallback().sendPingRequest();
		super.writePingRequestRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestAcceptedEvent<ID> e) {
		getMonitorCallback().sendLoginRequestAccepted();
		super.writeLoginRequestAcceptedRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestRejectedEvent<ID> e) {
		getMonitorCallback().sendLoginRequestRejected();
		super.writeLoginRequestRejectedRequested(ctx, e);
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
}
