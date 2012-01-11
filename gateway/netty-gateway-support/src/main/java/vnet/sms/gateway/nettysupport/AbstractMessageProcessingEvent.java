/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public abstract class AbstractMessageProcessingEvent<M extends Message>
        implements MessageProcessingEvent<M> {

	private final UUID	        id;

	private final long	        creationTimestamp;

	private final Channel	    channel;

	private final ChannelFuture	future;

	private final M	            failedMessage;

	private final SocketAddress	remoteAddress;

	protected AbstractMessageProcessingEvent(final Channel channel,
	        final M failedMessage, final SocketAddress remoteAddress) {
		this(UUID.randomUUID(), System.currentTimeMillis(), channel,
		        failedMessage, remoteAddress);
	}

	protected AbstractMessageProcessingEvent(final UUID id,
	        final long creationTimestamp, final Channel channel,
	        final M failedMessage, final SocketAddress remoteAddress) {
		notNull(id, "Argument 'id' must not be null");
		notNull(channel, "Argument 'channel' must not be null");
		notNull(failedMessage, "Argument 'failedMessage' must not be null");
		this.id = id;
		this.creationTimestamp = creationTimestamp;
		this.channel = channel;
		this.future = Channels.succeededFuture(channel);
		this.failedMessage = failedMessage;
		this.remoteAddress = remoteAddress;
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

	/**
	 * @see vnet.sms.gateway.nettysupport.MessageProcessingEvent#getId()
	 */
	@Override
	public UUID getId() {
		return this.id;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.MessageProcessingEvent#getMessage()
	 */
	@Override
	public M getMessage() {
		return this.failedMessage;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.MessageProcessingEvent#getRemoteAddress()
	 */
	@Override
	public SocketAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.MessageProcessingEvent#getCreationTimestamp()
	 */
	@Override
	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.MessageProcessingEvent#getCreationTime()
	 */
	@Override
	public Date getCreationTime() {
		return new Date(this.creationTimestamp);
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
		final AbstractMessageProcessingEvent<? extends Message> other = (AbstractMessageProcessingEvent<? extends Message>) obj;
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
		return getClass().getSimpleName() + "@" + this.hashCode() + " [id: "
		        + this.id + "|creationTimestamp: " + this.creationTimestamp
		        + "|channel: " + this.channel + "|failedMessage: "
		        + this.failedMessage + "|remoteAddress: " + this.remoteAddress
		        + "]";
	}
}
