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
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This is effectively a singleton.
 * </p>
 * 
 * @author obergner
 * 
 */
class ManagedChannelRegistry {

	private final Logger	                             log	              = LoggerFactory
	                                                                                  .getLogger(getClass());

	private final ConcurrentMap<Channel, ManagedChannel>	monitorPerChannel	= new ConcurrentHashMap<Channel, ManagedChannel>();

	void registerChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		if (this.monitorPerChannel.putIfAbsent(channel, new ManagedChannel(
		        channel)) == null) {
			addMandatoryChannelMonitor(this.monitorPerChannel.get(channel)
			        .getMonitor(), channel);

			channel.getCloseFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future)
				        throws Exception {
					unregisterChannel(channel);
				}
			});
			this.log.debug(
			        "Registered channel [%s] as a managed channel to be monitored via JMX",
			        channel);
		}
	}

	private void addMandatoryChannelMonitor(
	        final ChannelMonitor channelMonitor, final Channel channel)
	        throws IllegalArgumentException {
		final ChannelPipeline pipeline = channel.getPipeline();
		boolean monitorEnabledChannelHandlerFound = false;
		for (final ChannelHandler handler : pipeline.toMap().values()) {
			if (handler instanceof MonitoredChannel) {
				monitorEnabledChannelHandlerFound = true;
				final MonitoredChannel monitoringHandler = MonitoredChannel.class
				        .cast(handler);
				monitoringHandler.addMonitor(channelMonitor);
				this.log.trace(
				        "Added channel monitor [%s] to channel handler [%s]",
				        channelMonitor, monitoringHandler);
			}
		}
		if (!monitorEnabledChannelHandlerFound) {
			throw new IllegalArgumentException(
			        "The pipeline ["
			                + pipeline
			                + "] attached to channel ["
			                + channel
			                + "] does not contain a ChannelHandler that implements ["
			                + MonitoredChannel.class.getName()
			                + "]. This ManagedChannelRegistry requires each of its Channels to be monitoring-enabled.");
		}
	}

	void unregisterChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		final ManagedChannel removedManagedChannel = this.monitorPerChannel
		        .remove(channel);
		if (removedManagedChannel == null) {
			throw new IllegalArgumentException("Channel [" + channel
			        + "] to be unregistered has not been registered");
		}
		final ChannelPipeline pipeline = channel.getPipeline();
		for (final ChannelHandler handler : pipeline.toMap().values()) {
			if (handler instanceof MonitoredChannel) {
				final MonitoredChannel monitoringHandler = MonitoredChannel.class
				        .cast(handler);
				monitoringHandler.removeMonitor(removedManagedChannel
				        .getMonitor());
				this.log.trace(
				        "Removed channel monitor [%s] from channel handler [%s]",
				        removedManagedChannel.getMonitor(), monitoringHandler);
			}
		}
		this.log.debug(
		        "Unregistered channel [%s] as a managed channel - it will cease to be monitored via JMX",
		        channel);
	}
}
