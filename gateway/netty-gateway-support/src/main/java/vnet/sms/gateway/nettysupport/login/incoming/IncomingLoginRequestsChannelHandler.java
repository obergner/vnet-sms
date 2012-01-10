/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;

/**
 * @author obergner
 * 
 */
public class IncomingLoginRequestsChannelHandler<ID extends Serializable>
        extends UpstreamWindowedChannelHandler<ID> {

	public static final String	                  NAME	                   = "vnet.sms.gateway:incoming-login-handler";

	private final AuthenticationManager	          authenticationManager;

	private final AtomicReference<Authentication>	authenticatedClient	   = new AtomicReference<Authentication>();

	private final long	                          failedLoginResponseDelayMillis;

	private final Timer	                          failedLoginResponseTimer	= new HashedWheelTimer();

	public IncomingLoginRequestsChannelHandler(
	        final AuthenticationManager authenticationManager,
	        final long failedLoginResponseDelayMillis) {
		notNull(authenticationManager,
		        "Argument 'authenticationManager' must not be null");
		this.authenticationManager = authenticationManager;
		this.failedLoginResponseDelayMillis = failedLoginResponseDelayMillis;
	}

	@Override
	public void windowedMessageReceived(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e) {
		getLog().debug("Processing {} ...", e);
		if (e instanceof LoginRequestReceivedEvent) {
			processLoginRequest(ctx, (LoginRequestReceivedEvent<ID>) e);
		} else {
			processNonLoginRequestMessage(ctx, e);
		}
		getLog().debug("Finished processing {}", e);
	}

	private void processLoginRequest(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		getLog().info(
		        "Attempting to authenticate current channel {} using credentials from {} ...",
		        ctx.getChannel(), e);
		if (isCurrentChannelAuthenticated()) {
			getLog().warn(
			        "Ignoring attempt to re-authenticate an already authenticated channel {}",
			        ctx.getChannel());
			return;
		}

		final Authentication authentication = authenticate(e);
		if (authentication != null) {
			processSuccessfulAuthentication(ctx, e, authentication);
		} else {
			processFailedAuthentication(ctx, e);
		}

		// Send LoginRequest further upstream - it might be needed for auditing,
		// logging, metrics ...
		ctx.sendUpstream(e);
	}

	private void processSuccessfulAuthentication(
	        final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e,
	        final Authentication authentication) {
		if (!this.authenticatedClient.compareAndSet(null, authentication)) {
			getLog().warn(
			        "Ignoring attempt to re-authenticate an already authenticated channel {}",
			        ctx.getChannel());
		} else {
			getLog().info(
			        "Successfully authenticated channel {} - authenticated user is {}",
			        ctx.getChannel(), authentication.getPrincipal());
			ctx.sendDownstream(LoginRequestAcceptedEvent.accept(e));
			// Inform the wider community ...
			ctx.sendUpstream(new ChannelSuccessfullyAuthenticatedEvent(ctx
			        .getChannel(), e.getMessage()));
		}
	}

	private void processFailedAuthentication(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		getLog().warn(
		        "Authentication using credentials from {} failed - will delay negative response for [{}] milliseconds to prevent DoS attacks",
		        e, this.failedLoginResponseDelayMillis);
		this.failedLoginResponseTimer.newTimeout(
		        this.new DelayFailedLoginResponse(ctx, e),
		        this.failedLoginResponseDelayMillis, TimeUnit.MILLISECONDS);
	}

	private final class DelayFailedLoginResponse implements TimerTask {

		private final ChannelHandlerContext		    ctx;

		private final LoginRequestReceivedEvent<ID>	rejectedLogin;

		DelayFailedLoginResponse(final ChannelHandlerContext ctx,
		        final LoginRequestReceivedEvent<ID> rejectedLogin) {
			this.ctx = ctx;
			this.rejectedLogin = rejectedLogin;
		}

		@Override
		public void run(final Timeout timeout) throws Exception {
			if (timeout.isCancelled() || !this.ctx.getChannel().isOpen()) {
				return;
			}
			getLog().warn(
			        "Sending response to failed login request {} after delay of {} milliseconds",
			        this.rejectedLogin.getMessage(),
			        IncomingLoginRequestsChannelHandler.this.failedLoginResponseDelayMillis);
			this.ctx.sendDownstream(LoginRequestRejectedEvent
			        .reject(this.rejectedLogin));
		}
	}

	private Authentication authenticate(
	        final LoginRequestReceivedEvent<ID> loginRequestEvent) {
		try {
			return this.authenticationManager
			        .authenticate(new UsernamePasswordAuthenticationToken(
			                loginRequestEvent.getMessage().getUsername(),
			                loginRequestEvent.getMessage().getPassword()));
		} catch (final BadCredentialsException e) {
			getLog().warn(
			        "Login attempt by ["
			                + loginRequestEvent.getMessage().getUsername()
			                + "] failed: " + e.getMessage(), e);
			return null;
		}
	}

	private void processNonLoginRequestMessage(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e) {
		if (isCurrentChannelAuthenticated()) {
			getLog().trace(
			        "Received non-login request {} on authenticated channel {} - will propagate event further upstream",
			        e, ctx.getChannel());
			ctx.sendUpstream(e);
		} else {
			getLog().warn(
			        "Received non-login request {} on UNAUTHENTICATED channel {} - DISCARD",
			        e, ctx.getChannel());
			ctx.sendDownstream(NonLoginMessageReceivedOnUnauthenticatedChannelEvent
			        .discardNonLoginMessage(e));
		}
	}

	private boolean isCurrentChannelAuthenticated() {
		return this.authenticatedClient.get() != null;
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		getLog().info(
		        "Channel {} has been disconnected - stopping timer for delaying failed login responses",
		        e.getChannel());
		this.failedLoginResponseTimer.stop();
		super.channelDisconnected(ctx, e);
	}
}
