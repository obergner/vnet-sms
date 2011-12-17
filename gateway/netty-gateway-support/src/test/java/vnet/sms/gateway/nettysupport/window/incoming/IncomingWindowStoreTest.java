package vnet.sms.gateway.nettysupport.window.incoming;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettysupport.PingRequestReceivedEvent;

public class IncomingWindowStoreTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullOwnerUid() {
		new IncomingWindowStore<Long>(null, 1, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsEmptyOwnerUid() {
		new IncomingWindowStore<Long>("", 1, 1);
	}

	@Test
	public final void assertThatGetCurrentMessageCountReturnsNumberOfCurrentlyStoredMessages()
	        throws IllegalArgumentException, InterruptedException {
		final int expectedNumberOfMessages = 234;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatGetCurrentMessageCountReturnsNumberOfCurrentlyStoredMessages",
		        10000, 10);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i <= expectedNumberOfMessages; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		assertEquals(
		        "getCurrentMessageCount() did not return the correct number of currently stored messages",
		        expectedNumberOfMessages,
		        objectUnderTest.getCurrentMessageCount());
	}

	@Test
	public final void assertThatTryAcquireWindowSucceedsIfAWindowIsAvailable()
	        throws IllegalArgumentException, InterruptedException {
		final int windowStoreCapacity = 234;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatTryAcquireWindowSucceedsIfAWindowIsAvailable",
		        windowStoreCapacity, 10);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i < windowStoreCapacity; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		final PingRequest pingRequest = new PingRequest(
		        new InetSocketAddress(0), new InetSocketAddress(0));
		final boolean windowAcquired = objectUnderTest
		        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(Integer
		                .valueOf(windowStoreCapacity),
		                new UpstreamMessageEvent(mockChannel, pingRequest,
		                        new InetSocketAddress(0)), pingRequest));

		assertTrue(
		        "tryAcquireWindow(...) failed although a window is still available",
		        windowAcquired);
	}

	@Test
	public final void assertThatTryAcquireWindowFailsIfNoWindowIsAvailable()
	        throws IllegalArgumentException, InterruptedException {
		final int windowStoreCapacity = 234;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatTryAcquireWindowFailsIfNoWindowIsAvailable",
		        windowStoreCapacity, 10);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i <= windowStoreCapacity; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		final PingRequest pingRequest = new PingRequest(
		        new InetSocketAddress(0), new InetSocketAddress(0));
		final boolean windowAcquired = objectUnderTest
		        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(Integer
		                .valueOf(windowStoreCapacity + 1),
		                new UpstreamMessageEvent(mockChannel, pingRequest,
		                        new InetSocketAddress(0)), pingRequest));

		assertFalse(
		        "tryAcquireWindow(...) succeeded although no window is available",
		        windowAcquired);
	}

	@Test
	public final void assertThatTryAcquireWindowWaitsConfiguredTimespanForAWindowToBecomeAvailable()
	        throws IllegalArgumentException, InterruptedException {
		final int windowStoreCapacity = 234;
		final int waitTimeMillis = 500;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatTryAcquireWindowWaitsConfiguredTimespanForAWindowToBecomeAvailable",
		        windowStoreCapacity, waitTimeMillis);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i <= windowStoreCapacity; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		final PingRequest pingRequest = new PingRequest(
		        new InetSocketAddress(0), new InetSocketAddress(0));
		final long before = System.currentTimeMillis();
		objectUnderTest.tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
		        Integer.valueOf(windowStoreCapacity + 1),
		        new UpstreamMessageEvent(mockChannel, pingRequest,
		                new InetSocketAddress(0)), pingRequest));
		final long after = System.currentTimeMillis();

		assertTrue("tryAcquireWindow(...) did not wait for " + waitTimeMillis
		        + " ms for a window to become available",
		        (after - before) > (waitTimeMillis - 100));
	}

	@Test
	public final void assertThatReleaseWindowFreesAWindow()
	        throws IllegalArgumentException, InterruptedException {
		final Integer freedWindowId = Integer.valueOf(23);
		final int windowStoreCapacity = 234;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatReleaseWindowFreesAWindow", windowStoreCapacity, 10);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i <= windowStoreCapacity; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		objectUnderTest.releaseWindow(freedWindowId);

		final PingRequest pingRequest = new PingRequest(
		        new InetSocketAddress(0), new InetSocketAddress(0));
		final boolean windowAcquired = objectUnderTest
		        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
		                freedWindowId, new UpstreamMessageEvent(mockChannel,
		                        pingRequest, new InetSocketAddress(0)),
		                pingRequest));

		assertTrue("tryAcquireWindow(...) failed although the windowId "
		        + freedWindowId + " used has just been released",
		        windowAcquired);
	}

	@Test
	public final void assertThatShutdownReturnsPendingMessages()
	        throws IllegalArgumentException, InterruptedException {
		final int windowStoreCapacity = 234;
		final IncomingWindowStore<Integer> objectUnderTest = new IncomingWindowStore<Integer>(
		        "assertThatShutdownReturnsPendingMessages",
		        windowStoreCapacity, 10);

		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		for (int i = 1; i <= windowStoreCapacity; i++) {
			final PingRequest pingRequest = new PingRequest(
			        new InetSocketAddress(0), new InetSocketAddress(0));
			objectUnderTest
			        .tryAcquireWindow(new PingRequestReceivedEvent<Integer>(
			                Integer.valueOf(i), new UpstreamMessageEvent(
			                        mockChannel, pingRequest,
			                        new InetSocketAddress(0)), pingRequest));
		}

		final Map<Integer, Message> pendingMessages = objectUnderTest
		        .shutDown();

		assertEquals(
		        "shutDown() did not return the expected number of pending messages",
		        windowStoreCapacity, pendingMessages.size());
	}
}
