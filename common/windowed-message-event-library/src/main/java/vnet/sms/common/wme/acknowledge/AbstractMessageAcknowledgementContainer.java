/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
abstract class AbstractMessageAcknowledgementContainer<ID extends Serializable, M extends GsmPdu>
        implements MessageAcknowledgementContainer<ID, M>, Serializable {

	private static final long	   serialVersionUID	= 2256764884947681256L;

	private final MessageEventType	acknowledgedMessageType;

	private final Acknowledgement	acknowledgement;

	private final ID	           acknowledgedMessageReference;

	private final int	           receivingChannelId;

	private final M	               acknowledgedMessage;

	/**
	 * @param acknowledgedMessageType
	 * @param acknowledgement
	 * @param acknowledgedMessageReference
	 * @param acknowledgedMessage
	 */
	protected AbstractMessageAcknowledgementContainer(
	        final MessageEventType acknowledgedMessageType,
	        final Acknowledgement acknowledgement,
	        final ID acknowledgedMessageReference,
	        final int receivingChannelId, final M acknowledgedMessage) {
		notNull(acknowledgedMessageType,
		        "Argument 'acknowledgedMessageType' may not be null");
		notNull(acknowledgement, "Argument 'acknowledgement' may not be null");
		notNull(acknowledgedMessageReference,
		        "Argument 'acknowledgedMessageReference' may not be null");
		notNull(acknowledgedMessage,
		        "Argument 'acknowledgedMessage' may not be null");
		this.acknowledgedMessageType = acknowledgedMessageType;
		this.acknowledgement = acknowledgement;
		this.acknowledgedMessageReference = acknowledgedMessageReference;
		this.receivingChannelId = receivingChannelId;
		this.acknowledgedMessage = acknowledgedMessage;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#getAcknowledgedMessageType()
	 */
	@Override
	public MessageEventType getAcknowledgedMessageType() {
		return this.acknowledgedMessageType;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#getAcknowledgement()
	 */
	@Override
	public Acknowledgement getAcknowledgement() {
		return this.acknowledgement;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#isAccepted()
	 */
	@Override
	public boolean isAccepted() {
		return this.acknowledgement.is(Acknowledgement.Status.ACK);
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#getAcknowledgedMessageReference()
	 */
	@Override
	public ID getAcknowledgedMessageReference() {
		return this.acknowledgedMessageReference;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#getReceivingChannelId()
	 */
	@Override
	public int getReceivingChannelId() {
		return this.receivingChannelId;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer#getAcknowledgedMessage()
	 */
	@Override
	public M getAcknowledgedMessage() {
		return this.acknowledgedMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
		        * result
		        + ((this.acknowledgedMessage == null) ? 0
		                : this.acknowledgedMessage.hashCode());
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
		final AbstractMessageAcknowledgementContainer<?, ?> other = (AbstractMessageAcknowledgementContainer<?, ?>) obj;
		if (this.acknowledgedMessage == null) {
			if (other.acknowledgedMessage != null) {
				return false;
			}
		} else if (!this.acknowledgedMessage.equals(other.acknowledgedMessage)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + this.hashCode()
		        + "[acknowledgedMessageType: " + this.acknowledgedMessageType
		        + "|acknowledgement: " + this.acknowledgement
		        + "|acknowledgedMessageReference: "
		        + this.acknowledgedMessageReference + "|receivingChannelId: "
		        + this.receivingChannelId + "|acknowledgedMessage: "
		        + this.acknowledgedMessage + "]";
	}
}
