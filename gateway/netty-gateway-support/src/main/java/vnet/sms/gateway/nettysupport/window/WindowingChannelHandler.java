/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;

/**
 * @author obergner
 * 
 */
public class WindowingChannelHandler<ID extends Serializable> extends
        SimpleChannelHandler {

	private final Logger	              log	= LoggerFactory
	                                                  .getLogger(getClass());

	private final IncomingWindowStore<ID>	incomingWindowStore;

	private final MBeanServer	          mbeanServer;

	public WindowingChannelHandler(
	        final IncomingWindowStore<ID> incomingWindowStore,
	        final MBeanServer mbeanServer) {
		notNull(incomingWindowStore,
		        "Argument 'incomingWindowStore' cannot be null");
		this.incomingWindowStore = incomingWindowStore;
		this.mbeanServer = mbeanServer != null ? mbeanServer
		        : ManagementFactory.getPlatformMBeanServer();
	}

	@Override
	public final void messageReceived(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e instanceof WindowedMessageEvent)) {
			throw new IllegalStateException("Unsupported MessageEvent type: "
			        + e);
		}
		windowedMessageReceived(ctx,
		        (WindowedMessageEvent<ID, ? extends Message>) e);
	}

	public void windowedMessageReceived(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e)
	        throws IllegalArgumentException, InterruptedException {
		this.log.trace("Processing WindowedMessageEvent {}", e);
		if (this.incomingWindowStore.tryAcquireWindow(e)) {
			this.log.trace("Acquired free window for WindowedMessageEvent {}",
			        e);
			ctx.sendUpstream(e);
		} else {
			this.log.warn(
			        "No free window for WindowedMessageEvent {} available", e);
			ctx.sendUpstream(new NoWindowForIncomingMessageAvailableEvent(
			        (UpstreamMessageEvent) e, this.incomingWindowStore
			                .getMaximumCapacity(), this.incomingWindowStore
			                .getWaitTimeMillis()));
		}
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.mbeanServer.registerMBean(this.incomingWindowStore,
		        new ObjectName(this.incomingWindowStore.getObjectName()));
		this.log.info(
		        "Registered {} with MBeanServer {} using ObjectName [{}]",
		        new Object[] { this.incomingWindowStore, this.mbeanServer,
		                this.incomingWindowStore.getObjectName() });

		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);

		final Map<ID, Message> pendingMessages = this.incomingWindowStore
		        .shutDown();
		if (!pendingMessages.isEmpty()) {
			this.log.warn(
			        "Channel {} has been disconnected while {} still await acknowledgement - these messages will be DISCARDED",
			        ctx.getChannel(), pendingMessages.size());
			final PendingWindowedMessagesDiscardedEvent<ID> pendingMessagesDiscarded = new PendingWindowedMessagesDiscardedEvent<ID>(
			        ctx.getChannel(), pendingMessages);
			ctx.sendUpstream(pendingMessagesDiscarded);
		}

		this.mbeanServer.unregisterMBean(new ObjectName(
		        this.incomingWindowStore.getObjectName()));
		this.log.info("Removed {} from MBeanServer {}",
		        this.incomingWindowStore, this.mbeanServer);
	}
}
