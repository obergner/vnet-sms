package vnet.sms.gateway.server.framework.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingJmsMessageListener implements MessageListener {

	private static final Logger	LOG	= LoggerFactory
	                                        .getLogger(ForwardingJmsMessageListener.class);

	private static final class LoggingMessageListener implements
	        MessageListener {

		@Override
		public void onMessage(final Message arg0) {
			LOG.info("Received message {}", arg0);
		}

	}

	private final AtomicReference<MessageListener>	delegate	= new AtomicReference<MessageListener>(
	                                                                 new LoggingMessageListener());

	@Override
	public void onMessage(final Message arg0) {
		this.delegate.get().onMessage(arg0);
	}

	public MessageListener setDelegate(final MessageListener delegate) {
		return this.delegate.getAndSet(delegate != null ? delegate
		        : new LoggingMessageListener());
	}

	public MessageListener unsetDelegate() {
		return this.delegate.getAndSet(new LoggingMessageListener());
	}

	public CountDownLatch waitForMessage(final JmsMessagePredicate predicate) {
		final CountDownLatch matchingMessageReceived = new CountDownLatch(1);
		final MessageListener waitForMatchingMessage = new MessageListener() {

			@Override
			public void onMessage(final Message arg0) {
				LOG.debug("Testing if received message {} satisfies {} ...",
				        arg0, predicate);
				if (predicate.evaluate(arg0)) {
					LOG.info("Received message {} DOES satisfy {}", arg0,
					        predicate);
					matchingMessageReceived.countDown();
				} else {
					LOG.debug("Received message {} does NOT satisfy {}", arg0,
					        predicate);
				}
			}
		};
		setDelegate(waitForMatchingMessage);
		return matchingMessageReceived;
	}
}
