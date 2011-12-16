/**
 * 
 */
package vnet.routing.netty.server.support.window;

import static org.apache.commons.lang.Validate.notNull;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class WindowEnforcingChannelHandler extends SimpleChannelHandler
		implements LifeCycleAwareChannelHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final WindowStore windowStore;

	private final MBeanServer mbeanServer;

	public WindowEnforcingChannelHandler(final WindowStore windowStore,
			final MBeanServer mbeanServer) {
		notNull(windowStore, "Argument 'windowStore' cannot be null");
		this.windowStore = windowStore;
		this.mbeanServer = mbeanServer;
	}

	/**
	 * @see org.jboss.netty.channel.LifeCycleAwareChannelHandler#beforeAdd(org.jboss.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
		if (this.mbeanServer != null) {
			this.mbeanServer.registerMBean(this.windowStore, new ObjectName(
					this.windowStore.getObjectName()));
			this.log.info(
					"Registered {} with MBeanServer {} using ObjectName [{}]",
					new Object[] { this.windowStore, this.mbeanServer,
							this.windowStore.getObjectName() });
		}
	}

	/**
	 * @see org.jboss.netty.channel.LifeCycleAwareChannelHandler#afterAdd(org.jboss.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void afterAdd(final ChannelHandlerContext ctx) throws Exception {
		this.log.info("Added {} to ChannelPipeline {}", this.windowStore,
				ctx.getPipeline());
	}

	/**
	 * @see org.jboss.netty.channel.LifeCycleAwareChannelHandler#beforeRemove(org.jboss.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void beforeRemove(final ChannelHandlerContext ctx) throws Exception {
		if (this.mbeanServer != null) {
			this.mbeanServer.unregisterMBean(new ObjectName(this.windowStore
					.getObjectName()));
			this.log.info("Removed {} from MBeanServer {}", this.windowStore,
					this.mbeanServer);
		}
	}

	/**
	 * @see org.jboss.netty.channel.LifeCycleAwareChannelHandler#afterRemove(org.jboss.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void afterRemove(final ChannelHandlerContext ctx) throws Exception {
		this.log.info("Removed {} from ChannelPipeline {}", this.windowStore,
				ctx.getPipeline());
	}

}
