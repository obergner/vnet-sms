/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;
import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;

/**
 * @author obergner
 * 
 */
public abstract class DownstreamWindowedChannelHandler<ID extends Serializable>
        implements ChannelDownstreamHandler {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	/**
	 * {@inheritDoc} Down-casts the received downstream event into more
	 * meaningful sub-type event and calls an appropriate handler method with
	 * the down-casted event.
	 */
	@Override
	public final void handleDownstream(final ChannelHandlerContext ctx,
	        final ChannelEvent e) throws Exception {
		getLog().debug("Processing {} ...", e);
		if (e instanceof SendPingRequestEvent) {
			writePingRequestRequested(ctx, (SendPingRequestEvent<ID>) e);
		} else if (e instanceof ReceivedLoginRequestAckedEvent) {
			writeLoginRequestAcceptedRequested(ctx,
			        (ReceivedLoginRequestAckedEvent<ID>) e);
		} else if (e instanceof ReceivedLoginRequestNackedEvent) {
			writeLoginRequestRejectedRequested(ctx,
			        (ReceivedLoginRequestNackedEvent<ID>) e);
		} else if (e instanceof NonLoginMessageReceivedOnUnauthenticatedChannelEvent) {
			writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
			        ctx,
			        (NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?>) e);
		} else if (e instanceof SendPingRequestEvent) {
			writePingRequestRequested(ctx, (SendPingRequestEvent<ID>) e);
		} else if (e instanceof ChannelStateEvent) {
			final ChannelStateEvent evt = (ChannelStateEvent) e;
			switch (evt.getState()) {
			case OPEN:
				if (!Boolean.TRUE.equals(evt.getValue())) {
					closeRequested(ctx, evt);
				}
				break;
			case BOUND:
				if (evt.getValue() != null) {
					bindRequested(ctx, evt);
				} else {
					unbindRequested(ctx, evt);
				}
				break;
			case CONNECTED:
				if (evt.getValue() != null) {
					connectRequested(ctx, evt);
				} else {
					disconnectRequested(ctx, evt);
				}
				break;
			case INTEREST_OPS:
				setInterestOpsRequested(ctx, evt);
				break;
			default:
				ctx.sendDownstream(e);
			}
		} else {
			throw new IllegalStateException("Unsupported ChannelEvent [" + e
			        + "] - please add a handler method in "
			        + DownstreamWindowedChannelHandler.class.getName());
		}
		getLog().debug("Finished processing {}", e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) throws Exception {
		ctx.sendDownstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestAckedEvent<ID> e) throws Exception {
		ctx.sendDownstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestNackedEvent<ID> e) throws Exception {
		ctx.sendDownstream(e);
	}

	/**
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	protected void writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
	        final ChannelHandlerContext ctx,
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e)
	        throws Exception {
		ctx.sendDownstream(e);
	}

	/**
	 * Invoked when {@link Channel#bind(SocketAddress)} was called.
	 */
	public void bindRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);

	}

	/**
	 * Invoked when {@link Channel#connect(SocketAddress)} was called.
	 */
	public void connectRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);

	}

	/**
	 * Invoked when {@link Channel#setInterestOps(int)} was called.
	 */
	public void setInterestOpsRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);
	}

	/**
	 * Invoked when {@link Channel#disconnect()} was called.
	 */
	public void disconnectRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);

	}

	/**
	 * Invoked when {@link Channel#unbind()} was called.
	 */
	public void unbindRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);

	}

	/**
	 * Invoked when {@link Channel#close()} was called.
	 */
	public void closeRequested(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		ctx.sendDownstream(e);
	}

	protected Logger getLog() {
		return this.log;
	}
}
