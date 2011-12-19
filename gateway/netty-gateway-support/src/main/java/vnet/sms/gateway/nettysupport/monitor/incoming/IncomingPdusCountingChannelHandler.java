/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

/**
 * @author obergner
 * 
 */
public class IncomingPdusCountingChannelHandler<TP> extends
        SimpleChannelUpstreamHandler {

	public static final String	                           NAME	            = "vnet.sms.gateway:incoming-bytes-counting-handler";

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	private final Class<TP>	                               pduType;

	public IncomingPdusCountingChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry,
	        final Class<TP> pduType) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		notNull(pduType, "Argument 'pduType' must not be null");
		this.monitorRegistry = monitorRegistry;
		this.pduType = pduType;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!this.pduType.isInstance(e.getMessage())) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + this.pduType.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler AFTER any decoders but BEFORE any transport protocol converters?");
		}

		getMonitorCallback().pduReceived();

		super.messageReceived(ctx, e);
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
