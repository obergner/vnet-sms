/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notEmpty;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public class ReceivedSmsNackedEvent<ID extends java.io.Serializable> extends
        DownstreamReceivedMessageAcknowledgedEvent<ID, Sms> {

	private final int	 errorKey;

	private final String	errorDescription;

	private ReceivedSmsNackedEvent(final ID messageReference,
	        final Channel channel, final Sms message, final int errorKey,
	        final String errorDescription) {
		super(messageReference, MessageType.RECEIVED_SMS_NACKED, channel,
		        message, Acknowledgement.nack());
		notEmpty(errorDescription,
		        "Argument 'errorDescription' may be neither null nor empty");
		this.errorKey = errorKey;
		this.errorDescription = errorDescription;
	}

	/**
	 * @return the errorKey
	 */
	public final int getErrorKey() {
		return this.errorKey;
	}

	/**
	 * @return the errorDescription
	 */
	public final String getErrorDescription() {
		return this.errorDescription;
	}

	@Override
	public String toString() {
		return "ReceivedSmsNackedEvent@" + this.hashCode()
		        + "[acknowledgement: " + this.getAcknowledgement()
		        + "|messageReference: "
		        + this.getAcknowledgedMessageReference() + "|message: "
		        + this.getMessage() + "|channel: " + this.getChannel()
		        + "|errorKey: " + this.errorKey + "|errorDescription: "
		        + this.errorDescription + "]";
	}
}
