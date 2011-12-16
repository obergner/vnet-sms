/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.LoginRequestReceivedEvent;
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
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void windowedMessageReceived(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e) {
		super.windowedMessageReceived(ctx, e);
	}

	private void processLoginRequest(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) {
		// TODO Auto-generated method stub
		super.loginRequestReceived(ctx, e);
	}

	private void processNonLoginRequestMessage(final ChannelHandlerContext ctx,
	        final WindowedMessageEvent<ID, ? extends Message> e) {
	}

	private boolean isCurrentChannelAuthenticated() {
		final Authentication authClient = this.authenticatedClient.get();
		return (authClient != null) && authClient.isAuthenticated();
	}
}
