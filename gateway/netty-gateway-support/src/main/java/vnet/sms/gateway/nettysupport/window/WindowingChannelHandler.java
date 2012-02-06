/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;

/**
 * @author obergner
 * 
 */
public class WindowingChannelHandler<ID extends Serializable> extends
        SimpleChannelHandler {

	public static final String	          NAME	= "vnet.sms.gateway:incoming-outgoing-windowing-handler";

	private final Logger	              log	= LoggerFactory
	                                                   .getLogger(getClass());

	private final IncomingWindowStore<ID>	incomingWindowStore;

	public WindowingChannelHandler(
	        final IncomingWindowStore<ID> incomingWindowStore) {
		notNull(incomingWindowStore,
		        "Argument 'incomingWindowStore' cannot be null");
		this.incomingWindowStore = incomingWindowStore;
	}

	// ------------------------------------------------------------------------
	// Store incoming windowed messages in window store
	// ------------------------------------------------------------------------

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

	private void windowedMessageReceived(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e)
	        throws IllegalArgumentException, InterruptedException {
		this.log.trace("Processing {} ...", e);
		if (this.incomingWindowStore.tryAcquireWindow(e)) {
			this.log.trace("Acquired free window for {}", e);
			ctx.sendUpstream(e);
		} else {
			this.log.warn(
			        "No free window for {} available after waiting for {} milliseconds",
			        e, this.incomingWindowStore.getWaitTimeMillis());
			ctx.sendUpstream(new NoWindowForIncomingMessageAvailableEvent(
			        (UpstreamMessageEvent) e, this.incomingWindowStore
			                .getMaximumCapacity(), this.incomingWindowStore
			                .getWaitTimeMillis()));
		}
	}

	// ------------------------------------------------------------------------
	// Release previously stored windowed messages when receiving Acks/Nacks
	// ------------------------------------------------------------------------

	/**
	 * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		this.log.debug("Processing {} ...", e);
		if (e instanceof ReceivedMessageAcknowledgedEvent) {
			final ReceivedMessageAcknowledgedEvent<ID, ? extends Message> acknowledgedEvent = (ReceivedMessageAcknowledgedEvent<ID, ? extends Message>) e;
			releaseAcknowledgedMessage(ctx, acknowledgedEvent);
			ctx.sendDownstream(acknowledgedEvent);
		} else {
			super.writeRequested(ctx, e);
		}
		this.log.debug("Finished processing {}", e);
	}

	private void releaseAcknowledgedMessage(
	        final ChannelHandlerContext ctx,
	        final ReceivedMessageAcknowledgedEvent<ID, ? extends Message> acknowledgedEvent) {
		try {
			final Message acknowledgedMessage = acknowledgedEvent.getMessage();
			this.log.debug(
			        "Received acknowledgement {} for message {} - will release said message from incoming windowing store",
			        acknowledgedEvent.getAcknowledgement(), acknowledgedMessage);
			final ID acknowledgedMessageRef = acknowledgedEvent
			        .getAcknowledgedMessageReference();
			final Message releasedMessage = this.incomingWindowStore
			        .releaseWindow(acknowledgedMessageRef);
			if (!acknowledgedMessage.equals(releasedMessage)) {
				throw new IllegalArgumentException(
				        "The acknowledged message "
				                + acknowledgedMessage
				                + " is not the message "
				                + releasedMessage
				                + " that has been stored in this incoming windowing store under the same message reference ["
				                + acknowledgedMessageRef + "]");
			}
			this.log.debug("Released message {} from incoming windowing store",
			        releasedMessage);
		} catch (final Exception ex) {
			this.log.error("Failed to release acknowledged message "
			        + acknowledgedEvent.getMessage()
			        + " from incoming windowing store: " + ex.getMessage(), ex);
			ctx.sendUpstream(FailedToReleaseAcknowledgedMessageEvent.fail(
			        acknowledgedEvent, ex));
		}
	}

	// ------------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------------

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.incomingWindowStore.attachTo(e.getChannel());
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Map<ID, Message> pendingMessages = this.incomingWindowStore
		        .shutDown();
		if (!pendingMessages.isEmpty()) {
			this.log.warn(
			        "Channel {} has been disconnected while {} messages still await acknowledgement - these messages will be DISCARDED",
			        ctx.getChannel(), pendingMessages.size());
			final PendingWindowedMessagesDiscardedEvent<ID> pendingMessagesDiscarded = new PendingWindowedMessagesDiscardedEvent<ID>(
			        ctx.getChannel(), pendingMessages);
			ctx.sendUpstream(pendingMessagesDiscarded);
		}

		super.channelDisconnected(ctx, e);
	}
}
