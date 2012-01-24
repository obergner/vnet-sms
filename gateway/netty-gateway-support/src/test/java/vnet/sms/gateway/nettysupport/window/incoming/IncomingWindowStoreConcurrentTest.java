/**
 * 
 */
package vnet.sms.gateway.nettysupport.window.incoming;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.receive.SmsReceivedEvent;

/**
 * @author obergner
 * 
 */
public class IncomingWindowStoreConcurrentTest {

	/**
	 * Test method for
	 * {@link vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore#tryAcquireWindow(long, java.io.Serializable)}
	 * .
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public final void assertThatStoreMessageFillsWindowStoreToCapacity()
	        throws InterruptedException, ExecutionException {
		final int capacity = 10000;
		final long waitTimeMillis = 10L;
		final BlockingQueue<IdPlusMessage> queue = new ArrayBlockingQueue<IdPlusMessage>(
		        capacity);
		final Set<IdPlusMessage> backup = new HashSet<IdPlusMessage>(capacity);
		for (long key = 0; key < capacity; key++) {
			final IdPlusMessage idPlusMessage = new IdPlusMessage(key, new Sms(
			        "assertThatStoreMessageFillsWindowStoreToCapacity",
			        new InetSocketAddress(0), new InetSocketAddress(1)));
			queue.put(idPlusMessage);
			backup.add(idPlusMessage);
		}

		final IncomingWindowStore<Long> objectUnderTest = new IncomingWindowStore<Long>(
		        capacity, waitTimeMillis, new MBeanExporter());

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

		assertEquals(
		        "IncomingWindowStore should be completely filled, yet it isn't",
		        capacity, objectUnderTest.getCurrentMessageCount());
		for (final IdPlusMessage idPlusMessage : backup) {
			final Message storedMessage = objectUnderTest
			        .releaseWindow(idPlusMessage.id);
			assertNotNull("IncomingWindowStore should contain id "
			        + idPlusMessage.id + ", yet it doesn't", storedMessage);
			assertEquals("IncomingWindowStore stored wrong message under id "
			        + idPlusMessage.id, idPlusMessage.message, storedMessage);
		}
	}

	private static class IdPlusMessage {

		final long	id;

		final Sms	message;

		public IdPlusMessage(final long id, final Sms message) {
			this.id = id;
			this.message = message;
		}
	}

	private static class MessageStoreTask implements Runnable {

		private final IncomingWindowStore<Long>		objectUnderTest;

		private final BlockingQueue<IdPlusMessage>	queue;

		public MessageStoreTask(
		        final IncomingWindowStore<Long> objectUnderTest,
		        final BlockingQueue<IdPlusMessage> queue) {
			this.objectUnderTest = objectUnderTest;
			this.queue = queue;
		}

		@Override
		public void run() {
			try {
				final Channel mockChannel = createNiceMock(Channel.class);
				replay(mockChannel);
				while (true) {
					final IdPlusMessage next = this.queue.poll();
					if (next == null) {
						break;
					}

					final boolean success = this.objectUnderTest
					        .tryAcquireWindow(new SmsReceivedEvent<Long>(
					                next.id, new UpstreamMessageEvent(
					                        mockChannel, next, null),
					                next.message));
					if (!success) {
						throw new IllegalStateException(
						        "Failed to store message [id = " + next.id
						                + "|message = " + next.message + "]");
					}
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Test method for
	 * {@link vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore#tryAcquireWindow(long, java.io.Serializable)}
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
			final long waitTimeMillis = 10L;
			final int numberOfMessagesToStore = capacity + 1;
			final BlockingQueue<IdPlusMessage> queue = new ArrayBlockingQueue<IdPlusMessage>(
			        numberOfMessagesToStore);
			for (long key = 0; key < numberOfMessagesToStore; key++) {
				final IdPlusMessage idPlusMessage = new IdPlusMessage(key,
				        new Sms(String.valueOf(key), new InetSocketAddress(0),
				                new InetSocketAddress(1)));
				queue.put(idPlusMessage);
			}

			final IncomingWindowStore<Long> objectUnderTest = new IncomingWindowStore<Long>(
			        capacity, waitTimeMillis, new MBeanExporter());

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
