/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;

import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitors;
import vnet.sms.gateway.nettysupport.monitor.MonitoredChannel;

/**
 * @author obergner
 * 
 */
public class IncomingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends UpstreamWindowedChannelHandler<ID> implements MonitoredChannel {

	public static final String	  NAME	                    = "vnet.sms.gateway:incoming-messages-monitoring-handler";

	private final ChannelMonitors	channelMonitorCallbacks	= new ChannelMonitors();

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.LoginRequestReceivedEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.loginRequestReceived();
		super.loginRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.LoginResponseReceivedEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.loginResponseReceived();
		super.loginResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.PingRequestReceivedEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.pingRequestReceived();
		super.pingRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.PingResponseReceivedEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.pingResponseReceived();
		super.pingResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#smsReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.SmsReceivedEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) throws Exception {
		this.channelMonitorCallbacks.smsReceived();
		super.smsReceived(ctx, e);
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
