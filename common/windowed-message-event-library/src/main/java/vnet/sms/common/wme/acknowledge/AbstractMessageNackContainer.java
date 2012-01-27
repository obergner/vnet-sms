/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notEmpty;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
abstract class AbstractMessageNackContainer<ID extends Serializable, M extends Message>
        extends AbstractMessageAcknowledgementContainer<ID, M> implements
        MessageNackContainer<ID, M> {

	/**
     * 
     */
	private static final long	serialVersionUID	= -4417985555383260744L;

	private final int	      errorKey;

	private final String	  errorDescription;

	protected AbstractMessageNackContainer(final int errorKey,
	        final String errorDescription,
	        final MessageType acknowledgedMessageType,
	        final ID acknowledgedMessageReference,
	        final int receivingChannelId, final M acknowledgedMessage) {
		super(acknowledgedMessageType, Acknowledgement.nack(),
		        acknowledgedMessageReference, receivingChannelId,
		        acknowledgedMessage);
		notEmpty(errorDescription,
		        "Argument 'errorDescription' may be neither null nor empty. Got: "
		                + errorDescription);
		this.errorKey = errorKey;
		this.errorDescription = errorDescription;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageNackContainer#getErrorKey()
	 */
	@Override
	public int getErrorKey() {
		return this.errorKey;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.MessageNackContainer#getErrorDescription
	 *      ()
	 */
	@Override
	public String getErrorDescription() {
		return this.errorDescription;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + this.hashCode()
		        + "[errorKey: " + this.errorKey + "|errorDescription: "
		        + this.errorDescription + "|acknowledgedMessageType: "
		        + this.getAcknowledgedMessageType() + "|acknowledgement: "
		        + this.getAcknowledgement() + "|acknowledgedMessageReference: "
		        + this.getAcknowledgedMessageReference()
		        + "|receivingChannelId: " + this.getReceivingChannelId()
		        + "|acknowledgedMessage: " + this.getAcknowledgedMessage()
		        + "]";
	}
}
