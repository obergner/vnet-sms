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

import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
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
	 *      vnet.sms.common.wme.LoginRequestReceivedEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) throws Exception {
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
	 *      vnet.sms.common.wme.LoginResponseReceivedEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) throws Exception {
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
	 *      vnet.sms.common.wme.PingRequestReceivedEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) throws Exception {
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
	 *      vnet.sms.common.wme.PingResponseReceivedEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) throws Exception {
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
	 *      vnet.sms.common.wme.SmsReceivedEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) throws Exception {
		getLog().debug("Received {} - will notify [{}] listeners", e,
		        this.listeners.size());
		for (final IncomingMessagesListener<ID> listener : this.listeners) {
			listener.smsReceived(e);
		}
		super.smsReceived(ctx, e);
	}
}
