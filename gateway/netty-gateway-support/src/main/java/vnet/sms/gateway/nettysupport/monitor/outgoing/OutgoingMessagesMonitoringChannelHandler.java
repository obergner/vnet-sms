/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;

import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.WindowedChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitors;
import vnet.sms.gateway.nettysupport.monitor.MonitoredChannel;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends WindowedChannelHandler<ID> implements MonitoredChannel {

	public static final String	  NAME	                    = "vnet.sms.gateway:outgoing-messages-monitoring-handler";

	private final ChannelMonitors	channelMonitorCallbacks	= new ChannelMonitors();

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.sendPingRequest();
		super.writePingRequestRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestAcceptedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.sendLoginRequestAccepted();
		super.writeLoginRequestAcceptedRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestRejectedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.sendLoginRequestRejected();
		super.writeLoginRequestRejectedRequested(ctx, e);
	}

	@Override
	public void addMonitor(final ChannelMonitor monitor) {
		this.channelMonitorCallbacks.add(monitor);
	}

	@Override
	public void removeMonitor(final ChannelMonitor monitor) {
		this.channelMonitorCallbacks.remove(monitor);
	}

	@Override
	public void clearMonitors() {
		this.channelMonitorCallbacks.clear();
	}
}
