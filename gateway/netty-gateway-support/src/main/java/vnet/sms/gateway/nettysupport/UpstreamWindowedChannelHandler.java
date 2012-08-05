/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedSmsEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelAuthenticationFailedEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.PingResponseTimeoutExpiredEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.StartedToPingEvent;
import vnet.sms.gateway.nettysupport.window.FailedToReleaseAcknowledgedMessageEvent;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettysupport.window.PendingWindowedMessagesDiscardedEvent;

/**
 * @author obergner
 * 
 */
public abstract class UpstreamWindowedChannelHandler<ID extends Serializable>
        implements ChannelUpstreamHandler {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	/**
	 * {@inheritDoc} Down-casts the received upstream event into more meaningful
	 * sub-type event and calls an appropriate handler method with the
	 * down-casted event.
	 */
	@Override
	public final void handleUpstream(final ChannelHandlerContext ctx,
	        final ChannelEvent e) throws Exception {
		getLog().debug("Processing {} ...", e);
		if (e instanceof ReceivedLoginRequestEvent) {
			loginRequestReceived(ctx, (ReceivedLoginRequestEvent<ID>) e);
		} else if (e instanceof ReceivedLoginRequestAcknowledgementEvent) {
			loginResponseReceived(ctx, (ReceivedLoginRequestAcknowledgementEvent<ID>) e);
		} else if (e instanceof ReceivedPingRequestEvent) {
			pingRequestReceived(ctx, (ReceivedPingRequestEvent<ID>) e);
		} else if (e instanceof ReceivedPingRequestAcknowledgementEvent) {
			pingResponseReceived(ctx, (ReceivedPingRequestAcknowledgementEvent<ID>) e);
		} else if (e instanceof ReceivedSmsEvent) {
			smsReceived(ctx, (ReceivedSmsEvent<ID>) e);
		} else if (e instanceof ChannelAuthenticationFailedEvent) {
			channelAuthenticationFailed(ctx,
			        (ChannelAuthenticationFailedEvent) e);
		} else if (e instanceof ChannelSuccessfullyAuthenticatedEvent) {
			channelSuccessfullyAuthenticated(ctx,
			        (ChannelSuccessfullyAuthenticatedEvent) e);
		} else if (e instanceof StartedToPingEvent) {
			startedToPing(ctx, (StartedToPingEvent) e);
		} else if (e instanceof PingResponseTimeoutExpiredEvent) {
			pingResponseTimeoutExpired(ctx, (PingResponseTimeoutExpiredEvent) e);
		} else if (e instanceof NoWindowForIncomingMessageAvailableEvent) {
			noWindowForIncomingMessageAvailable(ctx,
			        (NoWindowForIncomingMessageAvailableEvent) e);
		} else if (e instanceof PendingWindowedMessagesDiscardedEvent) {
			pendingWindowedMessagesDiscarded(ctx,
			        (PendingWindowedMessagesDiscardedEvent<ID>) e);
		} else if (e instanceof FailedToReleaseAcknowledgedMessageEvent) {
			failedToReleaseAcknowledgedMessage(
			        ctx,
			        (FailedToReleaseAcknowledgedMessageEvent<ID, ? extends GsmPdu>) e);
		} else if (e instanceof WriteCompletionEvent) {
			final WriteCompletionEvent evt = (WriteCompletionEvent) e;
			writeComplete(ctx, evt);
		} else if (e instanceof ChildChannelStateEvent) {
			final ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
			if (evt.getChildChannel().isOpen()) {
				childChannelOpen(ctx, evt);
			} else {
				childChannelClosed(ctx, evt);
			}
		} else if (e instanceof ChannelStateEvent) {
			final ChannelStateEvent evt = (ChannelStateEvent) e;
			switch (evt.getState()) {
			case OPEN:
				if (Boolean.TRUE.equals(evt.getValue())) {
					channelOpen(ctx, evt);
				} else {
					channelClosed(ctx, evt);
				}
				break;
			case BOUND:
				if (evt.getValue() != null) {
					channelBound(ctx, evt);
				} else {
					channelUnbound(ctx, evt);
				}
				break;
			case CONNECTED:
				if (evt.getValue() != null) {
					channelConnected(ctx, evt);
				} else {
					channelDisconnected(ctx, evt);
				}
				break;
			case INTEREST_OPS:
				channelInterestChanged(ctx, evt);
				break;
			default:
				ctx.sendUpstream(e);
			}
		} else if (e instanceof ExceptionEvent) {
			exceptionCaught(ctx, (ExceptionEvent) e);
		} else {
			throw new IllegalStateException("Unsupported ChannelEvent [" + e
			        + "] - please add a handler method in "
			        + UpstreamWindowedChannelHandler.class.getName());
		}
		getLog().debug("Finished processing {}", e);
	}

	/**
	 * @param ctx
	 * @param e
	 */
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 */
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestAcknowledgementEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 */
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final ReceivedPingRequestEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 */
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final ReceivedPingRequestAcknowledgementEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 */
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final ReceivedSmsEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void channelAuthenticationFailed(final ChannelHandlerContext ctx,
	        final ChannelAuthenticationFailedEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void channelSuccessfullyAuthenticated(
	        final ChannelHandlerContext ctx,
	        final ChannelSuccessfullyAuthenticatedEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void startedToPing(final ChannelHandlerContext ctx,
	        final StartedToPingEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void pingResponseTimeoutExpired(final ChannelHandlerContext ctx,
	        final PingResponseTimeoutExpiredEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void noWindowForIncomingMessageAvailable(
	        final ChannelHandlerContext ctx,
	        final NoWindowForIncomingMessageAvailableEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void pendingWindowedMessagesDiscarded(
	        final ChannelHandlerContext ctx,
	        final PendingWindowedMessagesDiscardedEvent<ID> e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void failedToReleaseAcknowledgedMessage(
	        final ChannelHandlerContext ctx,
	        final FailedToReleaseAcknowledgedMessageEvent<ID, ? extends GsmPdu> e)
	        throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} is open, but not bound nor connected. <br/>
	 * 
	 * <strong>Be aware that this event is fired from within the Boss-Thread so
	 * you should not execute any heavy operation in there as it will block the
	 * dispatching to other workers!</strong>
	 */
	protected void channelOpen(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} is open and bound to a local address, but
	 * not connected. <br/>
	 * 
	 * <strong>Be aware that this event is fired from within the Boss-Thread so
	 * you should not execute any heavy operation in there as it will block the
	 * dispatching to other workers!</strong>
	 */
	protected void channelBound(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} is open, bound to a local address, and
	 * connected to a remote address. <br/>
	 * 
	 * <strong>Be aware that this event is fired from within the Boss-Thread so
	 * you should not execute any heavy operation in there as it will block the
	 * dispatching to other workers!</strong>
	 */
	protected void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel}'s {@link Channel#getInterestOps()
	 * interestOps} was changed.
	 */
	protected void channelInterestChanged(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} was disconnected from its remote peer.
	 */
	protected void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} was unbound from the current local
	 * address.
	 */
	protected void channelUnbound(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a {@link Channel} was closed and all its related resources
	 * were released.
	 */
	protected void channelClosed(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when something was written into a {@link Channel}.
	 */
	protected void writeComplete(final ChannelHandlerContext ctx,
	        final WriteCompletionEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a child {@link Channel} was open. (e.g. a server channel
	 * accepted a connection)
	 */
	protected void childChannelOpen(final ChannelHandlerContext ctx,
	        final ChildChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when a child {@link Channel} was closed. (e.g. the accepted
	 * connection was closed)
	 */
	protected void childChannelClosed(final ChannelHandlerContext ctx,
	        final ChildChannelStateEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Invoked when an exception was raised by an I/O thread or a
	 * {@link ChannelHandler}.
	 */
	protected void exceptionCaught(final ChannelHandlerContext ctx,
	        final ExceptionEvent e) throws Exception {
		if (this == ctx.getPipeline().getLast()) {
			getLog().warn(
			        "EXCEPTION, please implement " + getClass().getName()
			                + ".exceptionCaught() for proper handling.",
			        e.getCause());
		}
		ctx.sendUpstream(e);
	}

	protected Logger getLog() {
		return this.log;
	}
}
