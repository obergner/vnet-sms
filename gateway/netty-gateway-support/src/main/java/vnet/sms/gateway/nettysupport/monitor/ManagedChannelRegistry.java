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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportOperations;

import com.yammer.metrics.core.MetricsRegistry;

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

	private final ManagedChannel.Factory	             managedChannelFactory;

	ManagedChannelRegistry(final MBeanExportOperations mbeanExporter,
	        final MetricsRegistry metricsRegistry) {
		notNull(mbeanExporter,
		        "Argument 'managedChannelFactory' must not be null");
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.managedChannelFactory = ManagedChannel.factory(mbeanExporter,
		        metricsRegistry);
	}

	void registerChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		final ManagedChannel managedChannel = this.managedChannelFactory
		        .attachTo(channel);
		if (this.monitorPerChannel.putIfAbsent(channel, managedChannel) == null) {
			channel.getCloseFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future)
				        throws Exception {
					unregisterChannel(channel);
				}
			});
			this.log.debug(
			        "Registered channel {} as a managed channel to be monitored via JMX",
			        channel);
		} else {
			managedChannel.cleanup();
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
		this.log.debug(
		        "Unregistered channel {} as a managed channel - it will cease to be monitored via JMX",
		        channel);
	}
}
