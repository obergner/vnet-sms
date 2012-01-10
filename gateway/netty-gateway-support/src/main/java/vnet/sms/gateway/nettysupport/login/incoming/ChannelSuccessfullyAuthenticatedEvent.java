/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.gateway.nettysupport.IdentifiableChannelEvent;

/**
 * @author obergner
 * 
 */
public class ChannelSuccessfullyAuthenticatedEvent extends
        IdentifiableChannelEvent {

	private final LoginRequest	successfulLoginRequest;

	/**
	 * @param channel
	 * @param successfulLoginRequest
	 */
	ChannelSuccessfullyAuthenticatedEvent(final Channel channel,
	        final LoginRequest successfulLoginRequest) {
		super(channel);
		notNull(successfulLoginRequest,
		        "Argument 'successfulLoginRequest' must not be null");
		this.successfulLoginRequest = successfulLoginRequest;
	}

	/**
	 * @return the successfulLoginRequest
	 */
	public final LoginRequest getSuccessfulLoginRequest() {
		return this.successfulLoginRequest;
	}

	@Override
	public String toString() {
		return "ChannelSuccessfullyAuthenticatedEvent@" + this.hashCode()
		        + "[id: " + getId() + "|creationTimestamp: "
		        + getCreationTimestamp() + "|channel: " + getChannel()
		        + "|successfulLoginRequest: " + this.successfulLoginRequest
		        + "]";
	}
}
