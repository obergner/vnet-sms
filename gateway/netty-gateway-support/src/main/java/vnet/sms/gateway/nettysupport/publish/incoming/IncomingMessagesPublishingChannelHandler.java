/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;

import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedSmsEvent;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;

/**
 * @author obergner
 * 
 */
@ChannelHandler.Sharable
public class IncomingMessagesPublishingChannelHandler<ID extends Serializable>
        extends UpstreamWindowedChannelHandler<ID> {

	public static final String	                    NAME	  = "vnet.sms.gateway:incoming-messages-publishing-handler";

	private final Set<IncomingMessagesListener<ID>>	listeners	= new CopyOnWriteArraySet<IncomingMessagesListener<ID>>();

	public void addListener(final IncomingMessagesListener<ID> listener) {
		notNull(listener, "Argument 'listener' must not be null");
		this.listeners.add(listener);
		getLog().info("IncomingMessagesListener {} added", listener);
	}

	public void removeListener(final IncomingMessagesListener<ID> listener) {
		notNull(listener, "Argument 'listener' must not be null");
		this.listeners.remove(listener);
		getLog().info("IncomingMessagesListener {} removed", listener);
	}

	public void clearListeners() {
		getLog().info("Removing [{}] listeners", this.listeners.size());
		this.listeners.clear();
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.ReceivedLoginRequestEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.loginRequestReceived(e);
		}
		super.loginRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestAcknowledgementEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.loginResponseReceived(e);
		}
		super.loginResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.ReceivedPingRequestEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final ReceivedPingRequestEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.pingRequestReceived(e);
		}
		super.pingRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final ReceivedPingRequestAcknowledgementEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.pingResponseReceived(e);
		}
		super.pingResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#smsReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.ReceivedSmsEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final ReceivedSmsEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.smsReceived(e);
		}
		super.smsReceived(ctx, e);
	}
}
