package vnet.sms.gateway.server.framework.test;

import javax.jms.Destination;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

public class NoopJmsTemplate extends JmsTemplate {

	@Override
	public void convertAndSend(final Object message) throws JmsException {
	}

	@Override
	public void convertAndSend(final Destination destination,
	        final Object message) throws JmsException {
	}

	@Override
	public void convertAndSend(final String destinationName,
	        final Object message) throws JmsException {
	}

	@Override
	public void convertAndSend(final Object message,
	        final MessagePostProcessor postProcessor) throws JmsException {
	}

	@Override
	public void convertAndSend(final Destination destination,
	        final Object message, final MessagePostProcessor postProcessor)
	        throws JmsException {
	}

	@Override
	public void convertAndSend(final String destinationName,
	        final Object message, final MessagePostProcessor postProcessor)
	        throws JmsException {
	}
}
