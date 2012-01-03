/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

/**
 * @author obergner
 * 
 */
public class IncomingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends UpstreamWindowedChannelHandler<ID> {

	public static final String	                           NAME	            = "vnet.sms.gateway:incoming-messages-monitoring-handler";

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	public IncomingMessagesMonitoringChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		this.monitorRegistry = monitorRegistry;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.LoginRequestReceivedEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		getMonitorCallback().loginRequestReceived();
		super.loginRequestReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.LoginResponseReceivedEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) {
		getMonitorCallback().loginResponseReceived();
		super.loginResponseReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.PingRequestReceivedEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) {
		getMonitorCallback().pingRequestReceived();
		super.pingRequestReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.PingResponseReceivedEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) {
		getMonitorCallback().pingResponseReceived();
		super.pingResponseReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#smsReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.SmsReceivedEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) {
		getMonitorCallback().smsReceived();
		super.smsReceived(ctx, e);
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
