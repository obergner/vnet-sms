/**
 * 
 */
package vnet.sms.transports.serialization;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public final class ReferenceableMessageContainer implements Serializable {

	private static final long	serialVersionUID	= -278633444989175439L;

	public static final ReferenceableMessageContainer wrap(
	        final int messageReference, final Message message) {
		return new ReferenceableMessageContainer(messageReference, message);
	}

	private final int	  messageReference;

	private final Message	message;

	private ReferenceableMessageContainer(final int messageReference,
	        final Message message) {
		notNull(message, "Argument 'message' must not be null");
		this.messageReference = messageReference;
		this.message = message;
	}

	public int getMessageReference() {
		return this.messageReference;
	}

	public Message getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.message == null) ? 0 : this.message.hashCode());
		result = prime * result + this.messageReference;
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
		final ReferenceableMessageContainer other = (ReferenceableMessageContainer) obj;
		if (this.message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!this.message.equals(other.message)) {
			return false;
		}
		if (this.messageReference != other.messageReference) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReferenceableMessageContainer@" + hashCode()
		        + " [messageReference: " + this.messageReference + "|message: "
		        + this.message + "]";
	}
}
