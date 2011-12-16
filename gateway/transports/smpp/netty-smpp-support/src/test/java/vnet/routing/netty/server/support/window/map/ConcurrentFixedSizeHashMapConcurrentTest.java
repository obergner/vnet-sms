/**
 * 
 */
package vnet.routing.netty.server.support.window.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author obergner
 * 
 */
@Ignore("ConcurrentFixedSizeHashMap needs a complete overhaul")
public class ConcurrentFixedSizeHashMapConcurrentTest {

	private static class KeyValue {

		final long key;

		final String value;

		public KeyValue(final long key, final String value) {
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.map.ConcurrentFixedSizeHashMap#put(java.lang.Object, java.lang.Object)}
	 * .
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public final void assertThatPutFillsFixedSizeMapToCapacity()
			throws InterruptedException, ExecutionException {
		final int capacity = 10000;
		final BlockingQueue<KeyValue> queue = new ArrayBlockingQueue<KeyValue>(
				capacity);
		final Set<KeyValue> backup = new HashSet<KeyValue>(capacity);
		for (long key = 0; key < capacity; key++) {
			final KeyValue keyValue = new KeyValue(key, String.valueOf(key));
			queue.put(keyValue);
			backup.add(keyValue);
		}

		final ConcurrentFixedSizeHashMap<Long, String> objectUnderTest = new ConcurrentFixedSizeHashMap<Long, String>(
				capacity);

		final int numberOfTasks = 100;
		final Set<PutTask> putTasks = new HashSet<PutTask>(numberOfTasks);
		for (int i = 0; i < numberOfTasks; i++) {
			putTasks.add(new PutTask(objectUnderTest, queue));
		}

		final ExecutorService executor = Executors
				.newFixedThreadPool(numberOfTasks);
		final Set<Future<?>> completions = new HashSet<Future<?>>();
		for (final PutTask putTask : putTasks) {
			completions.add(executor.submit(putTask));
		}

		for (final Future<?> completion : completions) {
			completion.get();
		}
		executor.shutdown();
		executor.awaitTermination(3, TimeUnit.SECONDS);
		executor.shutdownNow();

		assertEquals(
				"ConcurrentFixedSizeHashMap should be completely filled, yet it isn't",
				capacity, objectUnderTest.size());
		for (final KeyValue keyValue : backup) {
			final String putValue = objectUnderTest.get(keyValue.key);
			assertNotNull("ConcurrentFixedSizeHashMap should contain id "
					+ keyValue.key + ", yet it doesn't", putValue);
			assertEquals(
					"ConcurrentFixedSizeHashMap stored wrong message under id "
							+ keyValue.key, keyValue.value, putValue);
		}
	}

	private static class PutTask implements Runnable {

		private final ConcurrentFixedSizeHashMap<Long, String> objectUnderTest;

		private final BlockingQueue<KeyValue> queue;

		public PutTask(
				final ConcurrentFixedSizeHashMap<Long, String> objectUnderTest,
				final BlockingQueue<KeyValue> queue) {
			this.objectUnderTest = objectUnderTest;
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				final KeyValue next = this.queue.poll();
				if (next == null) {
					break;
				}

				this.objectUnderTest.put(next.key, next.value);
			}
		}
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.map.ConcurrentFixedSizeHashMap#put(java.lang.Object, java.lang.Object)}
	 * .
	 * 
	 * @throws Throwable
	 */
	@Test(expected = MapCapacityExhaustedException.class)
	public final void assertThatPutRefusesToFillMapBeyondCapacity()
			throws Throwable {
		ExecutorService executor = null;
		try {
			final int capacity = 10000;
			final int numberOfKeyValuesToPut = capacity + 1;
			final BlockingQueue<KeyValue> queue = new ArrayBlockingQueue<KeyValue>(
					numberOfKeyValuesToPut);
			final Set<KeyValue> backup = new HashSet<KeyValue>(
					numberOfKeyValuesToPut);
			for (long key = 0; key < numberOfKeyValuesToPut; key++) {
				final KeyValue keyValue = new KeyValue(key, String.valueOf(key));
				queue.put(keyValue);
				backup.add(keyValue);
			}

			final ConcurrentFixedSizeHashMap<Long, String> objectUnderTest = new ConcurrentFixedSizeHashMap<Long, String>(
					capacity);

			final int numberOfTasks = 100;
			final Set<PutTask> putTasks = new HashSet<PutTask>(numberOfTasks);
			for (int i = 0; i < numberOfTasks; i++) {
				putTasks.add(new PutTask(objectUnderTest, queue));
			}

			executor = Executors.newFixedThreadPool(numberOfTasks);
			final Set<Future<?>> completions = new HashSet<Future<?>>();
			for (final PutTask putTask : putTasks) {
				completions.add(executor.submit(putTask));
			}

			for (final Future<?> completion : completions) {
				completion.get();
			}

			System.out.println(objectUnderTest.size());
			executor.shutdown();
			executor.awaitTermination(3, TimeUnit.SECONDS);
			executor.shutdownNow();
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof MapCapacityExhaustedException) {
				throw e.getCause();
			}
		} finally {
			if (executor != null) {
				executor.shutdown();
				executor.awaitTermination(3, TimeUnit.SECONDS);
				executor.shutdownNow();
			}
		}
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.map.ConcurrentFixedSizeHashMap#putIfAbsent(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test
	public final void testPutIfAbsent() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.window.map.ConcurrentFixedSizeHashMap#remove(java.lang.Object)}
	 * .
	 */
	@Test
	public final void testRemoveObject() {
		fail("Not yet implemented"); // TODO
	}

}
