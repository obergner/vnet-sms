/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.LoginRequestAcceptedEvent;
import vnet.sms.gateway.nettysupport.LoginRequestReceivedEvent;
import vnet.sms.gateway.nettysupport.LoginRequestRejectedEvent;
import vnet.sms.gateway.nettysupport.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.WindowedUpstreamChannelHandler;

/**
 * @author obergner
 * 
 */
public class IncomingLoginRequestsChannelHandler<ID extends Serializable>
        extends WindowedUpstreamChannelHandler<ID> {

	private final AuthenticationManager	          authenticationManager;

	private final AtomicReference<Authentication>	authenticatedClient	= new AtomicReference<Authentication>();

	public IncomingLoginRequestsChannelHandler(
	        final AuthenticationManager authenticationManager) {
		notNull(authenticationManager,
		        "Argument 'authenticationManager' must not be null");
		this.authenticationManager = authenticationManager;
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
		}
	}

	private void processFailedAuthentication(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		getLog().warn("Authentication using credentials from {} failed", e);
		ctx.sendDownstream(LoginRequestRejectedEvent.reject(e));
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
}
