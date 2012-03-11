/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public class ReceivedSmsNackedEvent<ID extends java.io.Serializable> extends
        DownstreamReceivedMessageAcknowledgedEvent<ID, Sms> {

	public static final <I extends Serializable> ReceivedSmsNackedEvent<I> convert(
	        final MessageEvent receivedSmsNackedMessageEvent) {
		notNull(receivedSmsNackedMessageEvent,
		        "Argument 'receivedSmsNackedMessageEvent' must not be null");
		isTrue(receivedSmsNackedMessageEvent.getMessage() instanceof ReceivedSmsNackedContainer,
		        "Can only convert MessageEvents having a ReceivedSmsNackedContainer as their payload. Got: "
		                + receivedSmsNackedMessageEvent.getMessage());
		final ReceivedSmsNackedContainer<I> nackContainer = ReceivedSmsNackedContainer.class
		        .cast(receivedSmsNackedMessageEvent.getMessage());
		return new ReceivedSmsNackedEvent<I>(
		        nackContainer.getAcknowledgedMessageReference(),
		        receivedSmsNackedMessageEvent.getChannel(),
		        receivedSmsNackedMessageEvent.getFuture(),
		        nackContainer.getAcknowledgedMessage(),
		        nackContainer.getErrorKey(),
		        nackContainer.getErrorDescription());
	}

	private final int	 errorKey;

	private final String	errorDescription;

	private ReceivedSmsNackedEvent(final ID messageReference,
	        final Channel channel, final ChannelFuture future,
	        final Sms message, final int errorKey, final String errorDescription) {
		super(messageReference, MessageEventType.RECEIVED_SMS_NACKED, channel,
		        future, message, Acknowledgement.nack());
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
