/**
 * 
 */
package vnet.sms.common.wme.receive;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public abstract class UpstreamWindowedMessageEvent<ID extends Serializable, M extends Message>
        extends UpstreamMessageEvent implements WindowedMessageEvent<ID, M> {

	private final ID	           messageReference;

	private final MessageEventType	type;

	protected UpstreamWindowedMessageEvent(final ID messageReference,
	        final MessageEventType type,
	        final UpstreamMessageEvent upstreamMessageEvent, final M message) {
		super(upstreamMessageEvent.getChannel(), message, upstreamMessageEvent
		        .getRemoteAddress());
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(type, "Argument 'type' must not be null");
		this.messageReference = messageReference;
		this.type = type;
	}

	/**
	 * @see vnet.sms.common.wme.WindowedMessageEvent#getMessageReference()
	 */
	@Override
	public ID getMessageReference() {
		return this.messageReference;
	}

	/**
	 * @see vnet.sms.common.wme.WindowedMessageEvent#getMessageType()
	 */
	@Override
	public MessageEventType getMessageType() {
		return this.type;
	}

	/**
	 * @see vnet.sms.common.wme.WindowedMessageEvent#getMessage()
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
		final UpstreamWindowedMessageEvent<? extends Serializable, ? extends Message> other = (UpstreamWindowedMessageEvent<? extends Serializable, ? extends Message>) obj;
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
		        + "[messageReference: " + this.messageReference + "|type: "
		        + getMessageType() + "|message: " + getMessage() + "|channel: "
		        + getChannel() + "|remoteAddress: " + getRemoteAddress() + "]";
	}

}
