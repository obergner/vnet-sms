/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.isTrue;
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
public class SendSmsAckEvent<ID extends Serializable> extends
        DownstreamSendMessageAcknowledgementEvent<ID, Sms> {

	public static final <I extends Serializable> SendSmsAckEvent<I> convert(
	        final MessageEvent receivedSmsAckedMessageEvent) {
		notNull(receivedSmsAckedMessageEvent,
		        "Argument 'receivedSmsAckedMessageEvent' must not be null");
		isTrue(receivedSmsAckedMessageEvent.getMessage() instanceof SendSmsAckContainer,
		        "Can only convert MessageEvents having a SendSmsAckContainer as their payload. Got: "
		                + receivedSmsAckedMessageEvent.getMessage());
		final SendSmsAckContainer<I> ackContainer = SendSmsAckContainer.class
		        .cast(receivedSmsAckedMessageEvent.getMessage());
		return new SendSmsAckEvent<I>(
		        ackContainer.getAcknowledgedMessageReference(),
		        receivedSmsAckedMessageEvent.getChannel(),
		        receivedSmsAckedMessageEvent.getFuture(),
		        ackContainer.getAcknowledgedMessage());
	}

	private SendSmsAckEvent(final ID messageReference, final Channel channel,
	        final ChannelFuture future, final Sms message) {
		super(messageReference, MessageEventType.SEND_SMS_ACK, channel,
		        message, Acknowledgement.ack());
	}
}
