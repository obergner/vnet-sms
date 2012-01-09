/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitors;
import vnet.sms.gateway.nettysupport.monitor.MonitoredChannel;

/**
 * @author obergner
 * 
 */
public class OutgoingBytesCountingChannelHandler extends SimpleChannelHandler
        implements MonitoredChannel {

	public static final String	  NAME	                    = "vnet.sms.gateway:outgoing-bytes-counting-handler";

	private final ChannelMonitors	channelMonitorCallbacks	= new ChannelMonitors();

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
		this.channelMonitorCallbacks.sendBytes(bytes.readableBytes());
		super.writeRequested(ctx, e);
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
