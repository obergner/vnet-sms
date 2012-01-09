/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitor;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitors;
import vnet.sms.gateway.nettysupport.monitor.MonitoredChannel;

/**
 * @author obergner
 * 
 */
public class IncomingBytesCountingChannelHandler extends
        SimpleChannelUpstreamHandler implements MonitoredChannel {

	public static final String	  NAME	                    = "vnet.sms.gateway:incoming-bytes-counting-handler";

	private final ChannelMonitors	channelMonitorCallbacks	= new ChannelMonitors();

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
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
		this.channelMonitorCallbacks.bytesReceived(bytes.readableBytes());

		super.messageReceived(ctx, e);
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
