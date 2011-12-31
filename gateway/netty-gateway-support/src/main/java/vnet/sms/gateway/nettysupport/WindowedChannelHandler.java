/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SendPingRequestEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.login.incoming.NonLoginMessageReceivedOnUnauthenticatedChannelEvent;

/**
 * @author obergner
 * 
 */
public class WindowedChannelHandler<ID extends Serializable> extends
        SimpleChannelHandler {

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
	}

	public void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	public void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	public void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	public void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	public void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) {
		ctx.sendUpstream(e);
	}

	@Override
	public final void writeRequested(final ChannelHandlerContext ctx,
	        final MessageEvent e) throws Exception {
		if (!(e instanceof WindowedMessageEvent)) {
			throw new IllegalStateException("Unsupported MessageEvent type: "
			        + e);
		}
		if (e instanceof SendPingRequestEvent) {
			writePingRequestRequested(ctx, (SendPingRequestEvent<ID>) e);
		} else if (e instanceof LoginRequestAcceptedEvent) {
			writeLoginRequestAcceptedRequested(ctx,
			        (LoginRequestAcceptedEvent<ID>) e);
		} else if (e instanceof LoginRequestRejectedEvent) {
			writeLoginRequestRejectedRequested(ctx,
			        (LoginRequestRejectedEvent<ID>) e);
		} else if (e instanceof NonLoginMessageReceivedOnUnauthenticatedChannelEvent) {
			writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
			        ctx,
			        (NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?>) e);
		} else if (e instanceof SendPingRequestEvent) {
			writePingRequestRequested(ctx, (SendPingRequestEvent<ID>) e);
		} else {
			throw new IllegalStateException("Add handler for message event: "
			        + e);
		}
	}

	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) {
		ctx.sendDownstream(e);
	}

	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestAcceptedEvent<ID> e) {
		ctx.sendDownstream(e);
	}

	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final LoginRequestRejectedEvent<ID> e) {
		ctx.sendDownstream(e);
	}

	protected void writeNonLoginMessageReceivedOnUnauthenticatedChannelRequested(
	        final ChannelHandlerContext ctx,
	        final NonLoginMessageReceivedOnUnauthenticatedChannelEvent<ID, ?> e) {
		ctx.sendDownstream(e);
	}

	protected Logger getLog() {
		return this.log;
	}
}
