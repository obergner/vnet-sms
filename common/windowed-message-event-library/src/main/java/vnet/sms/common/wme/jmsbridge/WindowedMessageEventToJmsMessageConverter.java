package vnet.sms.common.wme.jmsbridge;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedEvent;
import vnet.sms.common.wme.send.SendSmsEvent;

/**
 * @author obergner
 * 
 */
public class WindowedMessageEventToJmsMessageConverter implements
        MessageConverter {

	private final ChannelGroup	allConnectedChannels;

	public WindowedMessageEventToJmsMessageConverter(
	        final ChannelGroup allConnectedChannels) {
		notNull(allConnectedChannels,
		        "Argument 'allConnectedChannels' must not be null");
		this.allConnectedChannels = allConnectedChannels;
	}

	@Override
	public final javax.jms.Message toMessage(final Object object,
	        final Session session) throws JMSException,
	        MessageConversionException {
		if (!WindowedMessageEvent.class.isInstance(object)) {
			throw new IllegalArgumentException("Can only convert instances of "
			        + WindowedMessageEvent.class.getName() + ". Got: " + object);
		}

		final WindowedMessageEvent<? extends Serializable, ? extends Message> windowedMessageEvent = (WindowedMessageEvent<? extends Serializable, ? extends Message>) object;

		final ObjectMessage converted = session
		        .createObjectMessage(windowedMessageEvent.getMessage());

		converted.setJMSMessageID("urn:message:id:"
		        + windowedMessageEvent.getMessage().getId().toString());
		converted.setObjectProperty(Headers.MESSAGE_REFERENCE,
		        windowedMessageEvent.getAcknowledgedMessageReference());
		converted.setStringProperty(Headers.EVENT_TYPE, windowedMessageEvent
		        .getAcknowledgedMessageType().toString());
		converted.setIntProperty(Headers.RECEIVING_CHANNEL_ID,
		        windowedMessageEvent.getChannel().getId());
		converted.setLongProperty(Headers.RECEIVE_TIMESTAMP,
		        windowedMessageEvent.getMessage().getCreationTimestamp());
		converted.setStringProperty(Headers.SENDER_SOCKET_ADDRESS,
		        windowedMessageEvent.getMessage().getSender().toString());
		converted.setStringProperty(Headers.RECEIVER_SOCKET_ADDRESS,
		        windowedMessageEvent.getMessage().getReceiver().toString());

		return converted;
	}

	@Override
	public final Object fromMessage(final javax.jms.Message message)
	        throws JMSException, MessageConversionException {
		validateReceivedMessage(message);

		final ObjectMessage objectMessage = ObjectMessage.class.cast(message);
		final MessageType eventType = MessageType.valueOf(objectMessage
		        .getStringProperty(Headers.EVENT_TYPE));

		final MessageEvent converted;
		switch (eventType) {
		case RECEIVED_SMS_ACKED:
			final Sms ackedSms = Sms.class.cast(objectMessage.getObject());
			final Serializable messageReference = Serializable.class
			        .cast(objectMessage
			                .getObjectProperty(Headers.MESSAGE_REFERENCE));
			final int receivingChannelId = objectMessage
			        .getIntProperty(Headers.RECEIVING_CHANNEL_ID);
			final Channel receivingChannel = replyChannel(receivingChannelId);
			converted = new ReceivedSmsAckedEvent<Serializable>(
			        messageReference, receivingChannel, ackedSms);
			break;
		case RECEIVED_SMS_NACKED:
			final Sms nackedSms = Sms.class.cast(objectMessage.getObject());
			final Serializable messageRef = Serializable.class
			        .cast(objectMessage
			                .getObjectProperty(Headers.MESSAGE_REFERENCE));
			final int receivChannelId = objectMessage
			        .getIntProperty(Headers.RECEIVING_CHANNEL_ID);
			final Channel receivChannel = replyChannel(receivChannelId);
			final int errorKey = objectMessage
			        .getIntProperty(Headers.ERROR_KEY);
			final String errorDescription = objectMessage
			        .getStringProperty(Headers.ERROR_DESCRIPTION);
			converted = new ReceivedSmsNackedEvent<Serializable>(messageRef,
			        receivChannel, nackedSms, errorKey, errorDescription);
			break;
		case SEND_SMS:
			final Sms sms = Sms.class.cast(objectMessage.getObject());
			converted = new SendSmsEvent(this.allConnectedChannels.iterator()
			        .next(), sms);
			break;
		default:
			throw new IllegalArgumentException("Unsupported message type: "
			        + eventType);
		}
		return null;
	}

	private void validateReceivedMessage(final javax.jms.Message message)
	        throws IllegalArgumentException, JmsException {
		try {
			if (!ObjectMessage.class.isInstance(message)) {
				throw new IllegalArgumentException(
				        "Can only convert JMS messages of type "
				                + ObjectMessage.class.getName() + ". Got: "
				                + message);
			}
			final ObjectMessage objectMessage = ObjectMessage.class
			        .cast(message);
			final Serializable messagePayload = objectMessage.getObject();
			notNull(messagePayload, "No body/payload has been set on message "
			        + objectMessage);
			notNull(objectMessage.getStringProperty(Headers.EVENT_TYPE),
			        "No header '" + Headers.EVENT_TYPE
			                + "' has been set on message " + objectMessage);
			final MessageType eventType = MessageType.valueOf(objectMessage
			        .getStringProperty(Headers.EVENT_TYPE));
			switch (eventType) {
			case SEND_SMS:
				validateSendSms(objectMessage, messagePayload);
				break;
			case RECEIVED_SMS_ACKED:
				validateReceivedSmsAcked(objectMessage, messagePayload);
				break;
			case RECEIVED_SMS_NACKED:
				validateReceivedSmsNacked(objectMessage, messagePayload);
				break;
			default:
				throw new IllegalArgumentException(
				        "Unsupported message event type: " + eventType);
			}
		} catch (final JMSException e) {
			throw JmsUtils.convertJmsAccessException(e);
		}
	}

	private void validateSendSms(final ObjectMessage jmsMessage,
	        final Serializable messagePayload) {
		isTrue(Sms.class.isInstance(messagePayload), "Message '" + jmsMessage
		        + "' is of type " + MessageType.SEND_SMS
		        + ", yet it does not contain an SMS but rather "
		        + messagePayload);
	}

	private void validateReceivedSmsAcked(final ObjectMessage jmsMessage,
	        final Serializable messagePayload) throws JMSException {
		isTrue(Sms.class.isInstance(messagePayload), "Message '" + jmsMessage
		        + "' is of type " + MessageType.RECEIVED_SMS_ACKED
		        + ", yet it does not contain an SMS but rather "
		        + messagePayload);
		isTrue(jmsMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID) > 0,
		        "No header '" + Headers.RECEIVING_CHANNEL_ID
		                + "' has been set on message " + jmsMessage
		                + ". Cannot determine reply channel.");
		notNull(jmsMessage.getObjectProperty(Headers.MESSAGE_REFERENCE),
		        "No message reference has been set on message " + jmsMessage
		                + ". Cannot determine acked SMS.");
	}

	private void validateReceivedSmsNacked(final ObjectMessage jmsMessage,
	        final Serializable messagePayload) throws JMSException {
		isTrue(Sms.class.isInstance(messagePayload), "Message '" + jmsMessage
		        + "' is of type " + MessageType.RECEIVED_SMS_NACKED
		        + ", yet it does not contain an SMS but rather "
		        + messagePayload);
		isTrue(jmsMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID) > 0,
		        "No header '" + Headers.RECEIVING_CHANNEL_ID
		                + "' has been set on message " + jmsMessage
		                + ". Cannot determine reply channel.");
		notNull(jmsMessage.getObjectProperty(Headers.MESSAGE_REFERENCE),
		        "No message reference has been set on message " + jmsMessage
		                + ". Cannot determine nacked SMS.");
		isTrue(jmsMessage.getIntProperty(Headers.ERROR_KEY) > 0,
		        "No header '"
		                + Headers.ERROR_KEY
		                + "' has been set on message "
		                + jmsMessage
		                + ". This SMS has been nacked, and yet no error key has been provided.");
		notNull(jmsMessage.getObjectProperty(Headers.ERROR_DESCRIPTION),
		        "No header '"
		                + Headers.ERROR_DESCRIPTION
		                + "' has been set on message "
		                + jmsMessage
		                + ". This SMS has been nacked, and yet no error description has been provided.");
	}

	private Channel replyChannel(final int channelId)
	        throws MessageConversionException {
		final Channel replyChannel = this.allConnectedChannels.find(channelId);
		if (replyChannel == null) {
			throw new MessageConversionException(
			        "Could not find channel having ID = ["
			                + channelId
			                + "] among all currently connected channels. Maybe that channel has been closed subsequently?");
		}
		return replyChannel;
	}
}
