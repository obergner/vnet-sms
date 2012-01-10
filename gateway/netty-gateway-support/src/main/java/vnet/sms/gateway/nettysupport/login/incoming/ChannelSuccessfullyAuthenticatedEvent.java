/**
 * 
 */
package vnet.sms.gateway.nettysupport.login.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

import vnet.sms.common.messages.LoginRequest;

/**
 * @author obergner
 * 
 */
public class ChannelSuccessfullyAuthenticatedEvent implements ChannelEvent {

	private final UUID	        id;

	private final long	        creationTimestamp;

	private final Channel	    channel;

	private final ChannelFuture	future;

	private final LoginRequest	successfulLoginRequest;

	/**
	 * @param channel
	 * @param successfulLoginRequest
	 */
	ChannelSuccessfullyAuthenticatedEvent(final Channel channel,
	        final LoginRequest successfulLoginRequest) {
		notNull(channel, "Argument 'channel' must not be null");
		notNull(successfulLoginRequest,
		        "Argument 'successfulLoginRequest' must not be null");
		this.id = UUID.randomUUID();
		this.creationTimestamp = System.currentTimeMillis();
		this.channel = channel;
		this.future = Channels.succeededFuture(channel);
		this.successfulLoginRequest = successfulLoginRequest;
	}

	/**
	 * @return the id
	 */
	public final UUID getId() {
		return this.id;
	}

	/**
	 * @return the creationTimestamp
	 */
	public final long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public final Date getCreationTime() {
		return new Date(this.creationTimestamp);
	}

	/**
	 * @return the successfulLoginRequest
	 */
	public final LoginRequest getSuccessfulLoginRequest() {
		return this.successfulLoginRequest;
	}

	/**
	 * @see org.jboss.netty.channel.ChannelEvent#getChannel()
	 */
	@Override
	public Channel getChannel() {
		return this.channel;
	}

	/**
	 * @see org.jboss.netty.channel.ChannelEvent#getFuture()
	 */
	@Override
	public ChannelFuture getFuture() {
		return this.future;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ChannelSuccessfullyAuthenticatedEvent other = (ChannelSuccessfullyAuthenticatedEvent) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ChannelSuccessfullyAuthenticatedEvent@" + this.hashCode()
		        + "[id: " + this.id + "|channel: " + this.channel
		        + "|successfulLoginRequest: " + this.successfulLoginRequest
		        + "]";
	}
}
