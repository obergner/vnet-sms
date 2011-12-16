/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public class DownstreamWindowedMessageEvent<ID extends Serializable, M extends Message>
        extends DownstreamMessageEvent implements WindowedMessageEvent<ID, M> {

	private final ID	messageReference;

	protected DownstreamWindowedMessageEvent(final ID messageReference,
	        final DownstreamMessageEvent downstreamMessageEvent, final M message) {
		this(messageReference, downstreamMessageEvent.getChannel(),
		        downstreamMessageEvent.getFuture(), message,
		        downstreamMessageEvent.getRemoteAddress());
	}

	protected DownstreamWindowedMessageEvent(final ID messageReference,
	        final Channel channel, final ChannelFuture future,
	        final Object message, final SocketAddress remoteAddress) {
		super(channel, future, message, remoteAddress);
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(channel, "Argument 'channel' must not be null");
		notNull(future, "Argument 'future' must not be null");
		notNull(message, "Argument 'message' must not be null");
		this.messageReference = messageReference;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.WindowedMessageEvent#getMessageReference()
	 */
	@Override
	public ID getMessageReference() {
		return this.messageReference;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.WindowedMessageEvent#getMessage()
	 */
	@Override
	public M getMessage() {
		return (M) super.getMessage();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
		        * result
		        + ((this.messageReference == null) ? 0 : this.messageReference
		                .hashCode());
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
		final DownstreamWindowedMessageEvent<? extends Serializable, ? extends Message> other = (DownstreamWindowedMessageEvent<? extends Serializable, ? extends Message>) obj;
		if (this.messageReference == null) {
			if (other.messageReference != null) {
				return false;
			}
		} else if (!this.messageReference.equals(other.messageReference)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode()
		        + " [messageReference: " + this.messageReference + "|message: "
		        + getMessage() + "|channel: " + getChannel()
		        + "|remoteAddress: " + getRemoteAddress() + "]";
	}

}
