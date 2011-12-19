/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;

/**
 * @author obergner
 * 
 */
public class OutgoingBytesCountingChannelHandler extends SimpleChannelHandler {

	public static final String	                           NAME	            = "vnet.sms.gateway:outgoing-bytes-counting-handler";

	private final AtomicReference<ChannelMonitor.Callback>	monitorCallback	= new AtomicReference<ChannelMonitor.Callback>(
	                                                                                ChannelMonitor.Callback.NULL);

	private final ChannelMonitorRegistry	               monitorRegistry;

	public OutgoingBytesCountingChannelHandler(
	        final ChannelMonitorRegistry monitorRegistry) {
		notNull(monitorRegistry, "Argument 'monitorRegistry' must not be null");
		this.monitorRegistry = monitorRegistry;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof ChannelBuffer)) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + ChannelBuffer.class.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler BEFORE any decoders?");
		}

		final ChannelBuffer bytes = ChannelBuffer.class.cast(e.getMessage());
		getMonitorCallback().sendBytes(bytes.readableBytes());
		super.writeRequested(ctx, e);
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
		super.connectRequested(ctx, e);
	}
}
