/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

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
public class OutgoingPdusCountingChannelHandler<TP> extends
        SimpleChannelHandler implements MonitoredChannel {

	public static final String	  NAME	                    = "vnet.sms.gateway:outgoing-pdus-counting-handler";

	private final ChannelMonitors	channelMonitorCallbacks	= new ChannelMonitors();

	private final Class<TP>	      pduType;

	public OutgoingPdusCountingChannelHandler(final Class<TP> pduType) {
		notNull(pduType, "Argument 'pduType' must not be null");
		this.pduType = pduType;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!this.pduType.isInstance(e.getMessage())) {
			throw new IllegalStateException(
			        "Expected a message of type "
			                + this.pduType.getName()
			                + ", but got: "
			                + e.getMessage()
			                + ". Did you remember to insert this channel handler AFTER any transport protocol converters but BEFORE any encoders?");
		}

		this.channelMonitorCallbacks.sendPdu();

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
