package vnet.sms.common.wme.jmsbridge;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public class WindowedMessageEventToJmsMessageConverter implements
        MessageConverter {

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
		converted.setStringProperty(Headers.EVENT_TYPE, windowedMessageEvent
		        .getType().toString());
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
		throw new UnsupportedOperationException(
		        "fromMessage(Message m) is not yet supported");
	}
}
