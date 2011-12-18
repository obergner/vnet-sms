/**
 * 
 */
package vnet.routing.netty.server.support.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.routing.netty.server.support.window.spi.UnreleasedMessagesHandler;

/**
 * @author obergner
 * 
 */
public class WindowStoreConcurrentTest {

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.WindowStore#storeMessage(long, java.io.Serializable)}
	 * .
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public final void assertThatStoreMessageFillsWindowStoreToCapacity()
			throws InterruptedException, ExecutionException {
		final int capacity = 10000;
		final BlockingQueue<IdPlusMessage> queue = new ArrayBlockingQueue<IdPlusMessage>(
				capacity);
		final Set<IdPlusMessage> backup = new HashSet<IdPlusMessage>(capacity);
		for (long key = 0; key < capacity; key++) {
			final IdPlusMessage idPlusMessage = new IdPlusMessage(key,
					String.valueOf(key));
			queue.put(idPlusMessage);
			backup.add(idPlusMessage);
		}

		final WindowStore objectUnderTest = new WindowStore(
				"assertThatStoreMessageFillsWindowStoreToCapacity", capacity,
				new LoggingUnreleaseMessagesHandler());

		final int numberOfTasks = 100;
		final Set<MessageStoreTask> messageStoreTasks = new HashSet<MessageStoreTask>(
				numberOfTasks);
		for (int i = 0; i < numberOfTasks; i++) {
			messageStoreTasks.add(new MessageStoreTask(objectUnderTest, queue));
		}

		final ExecutorService executor = Executors
				.newFixedThreadPool(numberOfTasks);
		final Set<Future<?>> completions = new HashSet<Future<?>>();
		for (final MessageStoreTask messageStoreTask : messageStoreTasks) {
			completions.add(executor.submit(messageStoreTask));
		}

		for (final Future<?> completion : completions) {
			completion.get();
		}
		executor.shutdown();
		executor.awaitTermination(3, TimeUnit.SECONDS);
		executor.shutdownNow();

		assertEquals("WindowStore should be completely filled, yet it isn't",
				capacity, objectUnderTest.getCurrentMessageCount());
		for (final IdPlusMessage idPlusMessage : backup) {
			final String storedMessage = (String) objectUnderTest
					.releaseMessage(idPlusMessage.id);
			assertNotNull("WindowStore should contain id " + idPlusMessage.id
					+ ", yet it doesn't", storedMessage);
			assertEquals("WindowStore stored wrong message under id "
					+ idPlusMessage.id, idPlusMessage.message, storedMessage);
		}
	}

	private static class IdPlusMessage {

		final long id;

		final String message;

		public IdPlusMessage(final long id, final String message) {
			this.id = id;
			this.message = message;
		}
	}

	private static class LoggingUnreleaseMessagesHandler implements
			UnreleasedMessagesHandler {

		private final Logger log = LoggerFactory.getLogger(getClass());

		@Override
		public void onShutDown(final Map<Long, Serializable> unreleasedMessages) {
			this.log.info("Unreleased messages: {}", unreleasedMessages);
		}
	}

	private static class MessageStoreTask implements Runnable {

		private final WindowStore objectUnderTest;

		private final BlockingQueue<IdPlusMessage> queue;

		public MessageStoreTask(final WindowStore objectUnderTest,
				final BlockingQueue<IdPlusMessage> queue) {
			this.objectUnderTest = objectUnderTest;
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				final IdPlusMessage next = this.queue.poll();
				if (next == null) {
					break;
				}

				final boolean success = this.objectUnderTest.storeMessage(
						next.id, next.message);
				if (!success) {
					throw new IllegalStateException(
							"Failed to store message [id = " + next.id
									+ "|message = " + next.message + "]");
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.WindowStore#storeMessage(long, java.io.Serializable)}
	 * .
	 * 
	 * @throws Throwable
	 */
	@Test(expected = IllegalStateException.class)
	public final void assertThatStoreMessageRefusesToFillWindowStoreBeyondCapacity()
			throws Throwable {
		ExecutorService executor = null;
		try {
			final int capacity = 10000;
			final int numberOfMessagesToStore = capacity + 1;
			final BlockingQueue<IdPlusMessage> queue = new ArrayBlockingQueue<IdPlusMessage>(
					numberOfMessagesToStore);
			for (long key = 0; key < numberOfMessagesToStore; key++) {
				final IdPlusMessage idPlusMessage = new IdPlusMessage(key,
						String.valueOf(key));
				queue.put(idPlusMessage);
			}

			final WindowStore objectUnderTest = new WindowStore(
					"assertThatStoreMessageFillsWindowStoreToCapacity",
					capacity, new LoggingUnreleaseMessagesHandler());

			final int numberOfTasks = 100;
			final Set<MessageStoreTask> messageStoreTasks = new HashSet<MessageStoreTask>(
					numberOfTasks);
			for (int i = 0; i < numberOfTasks; i++) {
				messageStoreTasks.add(new MessageStoreTask(objectUnderTest,
						queue));
			}

			executor = Executors.newFixedThreadPool(numberOfTasks);
			final Set<Future<?>> completions = new HashSet<Future<?>>();
			for (final MessageStoreTask messageStoreTask : messageStoreTasks) {
				completions.add(executor.submit(messageStoreTask));
			}

			for (final Future<?> completion : completions) {
				completion.get();
			}
		} catch (final ExecutionException e) {
			throw e.getCause();
		} finally {
			if (executor != null) {
				executor.shutdown();
				executor.awaitTermination(3, TimeUnit.SECONDS);
				executor.shutdownNow();
			}
		}
	}
}
