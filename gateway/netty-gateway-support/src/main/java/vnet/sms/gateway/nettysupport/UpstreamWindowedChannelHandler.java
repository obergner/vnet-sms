/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public abstract class UpstreamWindowedChannelHandler<ID extends Serializable>
        extends SimpleChannelUpstreamHandler {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

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
	        final WindowedMessageEvent<ID, ? extends Message> e) {
		getLog().debug("Processing windowed message event {} ...", e);
		if (e instanceof LoginRequestReceivedEvent) {
			loginRequestReceived(ctx, (LoginRequestReceivedEvent<ID>) e);
		} else if (e instanceof LoginResponseReceivedEvent) {
			loginResponseReceived(ctx, (LoginResponseReceivedEvent<ID>) e);
		} else if (e instanceof PingRequestReceivedEvent) {
			pingRequestReceived(ctx, (PingRequestReceivedEvent<ID>) e);
		} else if (e instanceof PingResponseReceivedEvent) {
			pingResponseReceived(ctx, (PingResponseReceivedEvent<ID>) e);
		} else if (e instanceof SmsReceivedEvent) {
			smsReceived(ctx, (SmsReceivedEvent<ID>) e);
		} else {
			throw new IllegalStateException("Add handler for message event: "
			        + e);
		}
		getLog().debug("Finished processing windowed message event {}", e);
	}

	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	protected Logger getLog() {
		return this.log;
	}
}
