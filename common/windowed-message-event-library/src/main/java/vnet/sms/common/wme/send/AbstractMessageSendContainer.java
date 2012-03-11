package vnet.sms.common.wme.send;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.MessageEventType;

/**
 * @author obergner
 * 
 * @param <M>
 */
abstract class AbstractMessageSendContainer<M extends Message> implements
        MessageSendContainer<M>, Serializable {

	private static final long	   serialVersionUID	= -662855539360676935L;

	private final MessageEventType	messageEventType;

	private final M	               message;

	/**
	 * @param message
	 */
	protected AbstractMessageSendContainer(
	        final MessageEventType messageEventType, final M message) {
		notNull(messageEventType,
		        "Argument 'messageEventType' must not be null");
		notNull(message, "Argument 'message' must not be null");
		this.messageEventType = messageEventType;
		this.message = message;
	}

	/**
	 * @see vnet.sms.common.wme.send.MessageSendContainer#getMessage()
	 */
	@Override
	public M getMessage() {
		return this.message;
	}

	/**
	 * @see vnet.sms.common.wme.send.MessageSendContainer#getMessageType()
	 */
	@Override
	public MessageEventType getMessageType() {
		return this.messageEventType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.message == null) ? 0 : this.message.hashCode());
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
		final AbstractMessageSendContainer<?> other = (AbstractMessageSendContainer<?>) obj;
		if (this.message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!this.message.equals(other.message)) {
			return false;
		}
		return true;
	}
}
