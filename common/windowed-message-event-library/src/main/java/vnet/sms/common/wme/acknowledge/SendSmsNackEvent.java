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
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class SendSmsNackEvent<ID extends java.io.Serializable> extends
        DownstreamSendMessageAcknowledgementEvent<ID, Sms> {

	public static final <I extends Serializable> SendSmsNackEvent<I> convert(
	        final MessageEvent receivedSmsNackedMessageEvent) {
		notNull(receivedSmsNackedMessageEvent,
		        "Argument 'receivedSmsNackedMessageEvent' must not be null");
		isTrue(receivedSmsNackedMessageEvent.getMessage() instanceof SendSmsNackContainer,
		        "Can only convert MessageEvents having a SendSmsNackContainer as their payload. Got: "
		                + receivedSmsNackedMessageEvent.getMessage());
		final SendSmsNackContainer<I> nackContainer = SendSmsNackContainer.class
		        .cast(receivedSmsNackedMessageEvent.getMessage());
		return new SendSmsNackEvent<I>(
		        nackContainer.getAcknowledgedMessageReference(),
		        receivedSmsNackedMessageEvent.getChannel(),
		        receivedSmsNackedMessageEvent.getFuture(),
		        nackContainer.getAcknowledgedMessage(),
		        nackContainer.getErrorKey(),
		        nackContainer.getErrorDescription());
	}

	private final int	 errorKey;

	private final String	errorDescription;

	private SendSmsNackEvent(final ID messageReference, final Channel channel,
	        final ChannelFuture future, final Sms message, final int errorKey,
	        final String errorDescription) {
		super(messageReference, MessageEventType.SEND_SMS_NACK, channel,
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
		return "SendSmsNackEvent@" + this.hashCode() + "[acknowledgement: "
		        + this.getAcknowledgement() + "|messageReference: "
		        + this.getAcknowledgedMessageReference() + "|message: "
		        + this.getMessage() + "|channel: " + this.getChannel()
		        + "|errorKey: " + this.errorKey + "|errorDescription: "
		        + this.errorDescription + "]";
	}
}
