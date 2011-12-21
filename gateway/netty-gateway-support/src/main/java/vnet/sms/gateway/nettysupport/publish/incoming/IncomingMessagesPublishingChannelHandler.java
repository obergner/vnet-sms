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

import vnet.sms.gateway.nettysupport.LoginRequestReceivedEvent;
import vnet.sms.gateway.nettysupport.LoginResponseReceivedEvent;
import vnet.sms.gateway.nettysupport.PingRequestReceivedEvent;
import vnet.sms.gateway.nettysupport.PingResponseReceivedEvent;
import vnet.sms.gateway.nettysupport.SmsReceivedEvent;
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
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.gateway.nettysupport.LoginRequestReceivedEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.loginRequestReceived(e);
		}
		super.loginRequestReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.gateway.nettysupport.LoginResponseReceivedEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.loginResponseReceived(e);
		}
		super.loginResponseReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.gateway.nettysupport.PingRequestReceivedEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.pingRequestReceived(e);
		}
		super.pingRequestReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.gateway.nettysupport.PingResponseReceivedEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.pingResponseReceived(e);
		}
		super.pingResponseReceived(ctx, e);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#smsReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.gateway.nettysupport.SmsReceivedEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.smsReceived(e);
		}
		super.smsReceived(ctx, e);
	}
}
