/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.Channel;
import org.springframework.security.core.AuthenticationException;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.gateway.nettysupport.IdentifiableChannelEvent;

/**
 * @author obergner
 * 
 */
public class ChannelAuthenticationFailedEvent extends
        IdentifiableChannelEvent {

	private final LoginRequest	          failedLoginRequest;

	private final AuthenticationException	authenticationException;

	ChannelAuthenticationFailedEvent(final Channel channel,
	        final LoginRequest failedLoginRequest,
	        final AuthenticationException authenticationException) {
		super(channel);
		notNull(failedLoginRequest,
		        "Argument 'failedLoginRequest' must not be null");
		notNull(authenticationException,
		        "Argument 'authenticationException' must not be null");
		this.failedLoginRequest = failedLoginRequest;
		this.authenticationException = authenticationException;
	}

	/**
	 * @return the failedLoginRequest
	 */
	public final LoginRequest getFailedLoginRequest() {
		return this.failedLoginRequest;
	}

	/**
	 * @return the authenticationException
	 */
	public final AuthenticationException getAuthenticationException() {
		return this.authenticationException;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChannelAuthenticationFailedEvent@" + this.hashCode()
		        + "[id: " + getId() + "|creationTimestamp: "
		        + this.getCreationTimestamp() + "|getChannel: "
		        + this.getChannel() + "|failedLoginRequest: "
		        + this.failedLoginRequest + "|authenticationException: "
		        + this.authenticationException + "]";
	}
}
