/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * @author obergner
 * 
 */
public abstract class AbstractIdentifiableChannelEvent implements
        IdentifiableChannelEvent {

	private final UUID	        id;

	private final long	        creationTimestamp;

	private final Channel	    channel;

	private final ChannelFuture	future;

	/**
	 * @param channel
	 * @param future
	 */
	protected AbstractIdentifiableChannelEvent(final Channel channel) {
		this(channel, Channels.succeededFuture(channel));
	}

	/**
	 * @param channel
	 * @param future
	 */
	protected AbstractIdentifiableChannelEvent(final Channel channel,
	        final ChannelFuture future) {
		notNull(channel, "Argument 'channel' must not be null");
		notNull(future, "Argument 'future' must not be null");
		this.id = UUID.randomUUID();
		this.creationTimestamp = System.currentTimeMillis();
		this.channel = channel;
		this.future = future;
	}

	/**
	 * @return the id
	 */
	@Override
	public final UUID getId() {
		return this.id;
	}

	/**
	 * @return the creationTimestamp
	 */
	@Override
	public final long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	@Override
	public final Date getCreationTime() {
		return new Date(this.creationTimestamp);
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		final AbstractIdentifiableChannelEvent other = (AbstractIdentifiableChannelEvent) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
