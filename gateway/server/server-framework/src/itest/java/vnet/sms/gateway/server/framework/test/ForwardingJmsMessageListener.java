package vnet.sms.gateway.server.framework.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

	public CountDownLatch awaitMatchingMessage(
	        final JmsMessagePredicate predicate) {
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

	public Future<Message> receiveMatchingMessage(
	        final JmsMessagePredicate predicate) {
		final CountDownLatch matchingMessageReceived = new CountDownLatch(1);
		final AtomicReference<Message> receivedMatchingMessage = new AtomicReference<Message>();
		final MessageListener waitForMatchingMessage = new MessageListener() {

			@Override
			public void onMessage(final Message arg0) {
				LOG.debug("Testing if received message {} satisfies {} ...",
				        arg0, predicate);
				if (predicate.evaluate(arg0)) {
					LOG.info("Received message {} DOES satisfy {}", arg0,
					        predicate);
					receivedMatchingMessage.set(arg0);
					matchingMessageReceived.countDown();
				} else {
					LOG.debug("Received message {} does NOT satisfy {}", arg0,
					        predicate);
				}
			}
		};
		setDelegate(waitForMatchingMessage);

		return new JmsMessageFuture(matchingMessageReceived,
		        receivedMatchingMessage);
	}

	private final class JmsMessageFuture implements Future<Message> {
		private final CountDownLatch		   matchingMessageReceived;
		private final AtomicReference<Message>	receivedMatchingMessage;

		private JmsMessageFuture(final CountDownLatch matchingMessageReceived,
		        final AtomicReference<Message> receivedMatchingMessage) {
			this.matchingMessageReceived = matchingMessageReceived;
			this.receivedMatchingMessage = receivedMatchingMessage;
		}

		@Override
		public boolean cancel(final boolean arg0) {
			return false;
		}

		@Override
		public Message get() throws InterruptedException, ExecutionException {
			this.matchingMessageReceived.await();
			return this.receivedMatchingMessage.get();
		}

		@Override
		public Message get(final long arg0, final TimeUnit arg1)
		        throws InterruptedException, ExecutionException,
		        TimeoutException {
			if (!this.matchingMessageReceived.await(arg0, arg1)) {
				throw new TimeoutException(
				        "Failed to retrieve Message after waiting for [" + arg0
				                + "] " + arg1);
			}
			return this.receivedMatchingMessage.get();
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return this.matchingMessageReceived.getCount() == 0;
		}
	}
}
