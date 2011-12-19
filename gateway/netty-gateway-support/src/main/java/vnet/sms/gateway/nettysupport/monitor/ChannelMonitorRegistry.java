/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * <p>
 * This is effectively a singleton.
 * </p>
 * 
 * @author obergner
 * 
 */
public class ChannelMonitorRegistry {

	private final ConcurrentMap<Channel, ChannelMonitor>	monitorPerChannel	= new ConcurrentHashMap<Channel, ChannelMonitor>();

	public ChannelMonitor.Callback registerChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		if (this.monitorPerChannel.putIfAbsent(channel, new ChannelMonitor(
		        channel)) == null) {
			channel.getCloseFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future)
				        throws Exception {
					ChannelMonitorRegistry.this.monitorPerChannel
					        .remove(channel);
				}
			});
		}
		return this.monitorPerChannel.get(channel).getCallback();
	}

	public ChannelMonitor monitorForChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		return this.monitorPerChannel.get(channel);
	}
}
