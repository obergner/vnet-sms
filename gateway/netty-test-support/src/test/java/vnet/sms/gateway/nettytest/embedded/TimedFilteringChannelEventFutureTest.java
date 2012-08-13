package vnet.sms.gateway.nettytest.embedded;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import com.google.common.base.Predicate;

public class TimedFilteringChannelEventFutureTest {

	@Test
	public final void assertThatCancelReturnsFalse() {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));
		assertFalse(
		        "cancel() should always return false as TimedFilteringChannelEventFuture is not cancellable",
		        objectUnderTest.cancel(true));
	}

	@Test
	public final void assertThatGetReturnsMatchingMessageEventIfFutureCompletesSuccessfully()
	        throws InterruptedException, ExecutionException {
		final Object expectedPayload = new Object();
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final Channel mockChannel = createNiceMock(Channel.class);
		expect(mockChannel.getRemoteAddress()).andReturn(
		        new InetSocketAddress(1)).anyTimes();
		replay(mockChannel);
		final MessageEvent matchingMessageEvent = new UpstreamMessageEvent(
		        mockChannel, expectedPayload, null);

		objectUnderTest.acceptsChannelEvent(matchingMessageEvent);

		assertEquals(
		        "get() should return MessageEvent matching the supplied filter if a matching MessageEvent has been offered",
		        matchingMessageEvent, objectUnderTest.get().get());
	}

	@Test(expected = ExecutionException.class)
	public final void assertThatGetThrowsExecutionExceptionIfFutureCompletesUnsuccessfully()
	        throws InterruptedException, ExecutionException {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final ExceptionEvent exEvent = new DefaultExceptionEvent(
		        createNiceMock(Channel.class),
		        new RuntimeException(
		                "assertThatGetThrowsExecutionExceptionIfFutureCompletesUnsuccessfully"));
		objectUnderTest.acceptsExceptionEvent(exEvent);

		objectUnderTest.get().get();
	}

	@Test
	public final void assertThatTimedGetReturnsMatchingMessageEventIfFutureCompletesSuccessfully()
	        throws InterruptedException, ExecutionException, TimeoutException {
		final Object expectedPayload = new Object();
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final Channel mockChannel = createNiceMock(Channel.class);
		expect(mockChannel.getRemoteAddress()).andReturn(
		        new InetSocketAddress(1)).anyTimes();
		replay(mockChannel);
		final MessageEvent matchingMessageEvent = new UpstreamMessageEvent(
		        mockChannel, expectedPayload, null);

		objectUnderTest.acceptsChannelEvent(matchingMessageEvent);

		assertEquals(
		        "get(200L, MILLISECONDS) should return MessageEvent matching the supplied filter if a matching MessageEvent has been offered",
		        matchingMessageEvent, objectUnderTest.get(200L, MILLISECONDS)
		                .get());
	}

	@Test(expected = ExecutionException.class)
	public final void assertThatTimedGetThrowsExecutionExceptionIfFutureCompletesUnsuccessfully()
	        throws InterruptedException, ExecutionException, TimeoutException {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final ExceptionEvent exEvent = new DefaultExceptionEvent(
		        createNiceMock(Channel.class),
		        new RuntimeException(
		                "assertThatTimedGetThrowsExecutionExceptionIfFutureCompletesUnsuccessfully"));
		objectUnderTest.acceptsExceptionEvent(exEvent);

		objectUnderTest.get(200L, MILLISECONDS).get();
	}

	@Test
	public final void assertThatTimedGetThrowsTimeoutExceptionIfNoMatchIsOfferedWithinSpecifiedTimeout()
	        throws InterruptedException, ExecutionException {
		final long timeoutMillis = 200L;
		final long confidenceInterval = 30L;
		long before = 0L;
		try {
			final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
			        ChannelEventFilters.ofType(MessageEvent.class));

			before = System.currentTimeMillis();
			objectUnderTest.get(timeoutMillis, MILLISECONDS);
			fail("get(200L, MILLISECONDS) should have thrown TimeoutException");
		} catch (final TimeoutException e) {
			final long duration = System.currentTimeMillis() - before;
			assertTrue(
			        "get("
			                + timeoutMillis
			                + ", MILLISECONDS) did not time out within the specified timeout but "
			                + duration,
			        (timeoutMillis - confidenceInterval < duration)
			                && (timeoutMillis + confidenceInterval > duration));
		}
	}

	@Test
	public final void assertThatIsCancelledReturnsFalse() {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));
		assertFalse(
		        "isCancelled() should always return false as TimedFilteringChannelEventFuture<ChannelEvent> is not cancelable",
		        objectUnderTest.isCancelled());
	}

	@Test
	public final void asserThatIsDoneReturnsFalseAsLongAsNoMatchingMessageEventIsOffered() {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		assertFalse(
		        "isDone() should return false as long a no MessageEvent matching the supplied filter is offered",
		        objectUnderTest.isDone());
	}

	@Test
	public final void asserThatIsDoneReturnsTrueIfMatchingMessageEventHasBeenOffered() {
		final Object expectedPayload = new Object();
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final MessageEvent matchingMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), expectedPayload, null);

		objectUnderTest.acceptsChannelEvent(matchingMessageEvent);

		assertTrue(
		        "isDone() should return true as soon as a MessageEvent matching the supplied filter has been offered",
		        objectUnderTest.isDone());
	}

	@Test
	public final void asserThatIsDoneReturnsTrueIfExceptionEventHasBeenRecorded() {
		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        ChannelEventFilters.ofType(MessageEvent.class));

		final ExceptionEvent exEvent = new DefaultExceptionEvent(
		        createNiceMock(Channel.class),
		        new RuntimeException(
		                "asserThatIsDoneReturnsTrueIfExceptionEventHasBeenRecorded"));
		objectUnderTest.acceptsExceptionEvent(exEvent);

		assertTrue(
		        "isDone() should return true as soon as an ExceptionEvent has been recorded",
		        objectUnderTest.isDone());
	}

	@Test
	public final void assertThatWhenAccessedConcurrentlyOnlyOneThreadGetsToCompleteTimedFilteringChannelEventFuture()
	        throws InterruptedException, BrokenBarrierException,
	        ExecutionException {
		final int numberOfSuccessfulCompletors = 30;
		final int numberOfFailedCompletors = 20;
		final CyclicBarrier startBarrier = new CyclicBarrier(
		        numberOfSuccessfulCompletors + numberOfFailedCompletors + 1);
		final int numberOfRounds = 200;

		final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest = new TimedFilteringChannelEventFuture<ChannelEvent>(
		        new Predicate<ChannelEvent>() {
			        @Override
			        public boolean apply(final ChannelEvent event) {
				        return MessageEvent.class.isInstance(event)
				                && String.class.isInstance(MessageEvent.class
				                        .cast(event).getMessage());
			        }
		        });

		final ExecutorService exec = Executors
		        .newFixedThreadPool(numberOfSuccessfulCompletors
		                + numberOfFailedCompletors);
		final CompletionService<List<Completion>> completionService = new ExecutorCompletionService<List<Completion>>(
		        exec);
		for (int i = 0; i < numberOfFailedCompletors; i++) {
			completionService.submit(new FailedCompletor(objectUnderTest,
			        numberOfRounds, startBarrier));
		}
		for (int i = 0; i < numberOfSuccessfulCompletors; i++) {
			completionService.submit(new SuccessfulCompletor(objectUnderTest,
			        numberOfRounds, startBarrier));
		}
		startBarrier.await();

		final List<Completion> allCompletions = new ArrayList<Completion>(1);
		for (int i = 0; i < numberOfSuccessfulCompletors
		        + numberOfFailedCompletors; i++) {
			allCompletions.addAll(completionService.take().get());
		}

		assertEquals(
		        "Our TimedFilteringChannelEventFuture<ChannelEvent> should have been completed exactly once by one thread",
		        1, allCompletions.size());
		final Completion theCompletion = allCompletions.get(0);
		try {
			final ChannelEvent matchedMessageEvent = objectUnderTest.get()
			        .get();
			assertEquals(
			        "TimedFilteringChannelEventFuture should have stored the MessageEvent set by the Thread that saw 'true' returned from onMessageEvent(...)",
			        theCompletion.channelEvent.getMessage(), MessageEvent.class
			                .cast(matchedMessageEvent).getMessage());
		} catch (final ExecutionException e) {
			assertEquals(
			        "TimedFilteringChannelEventFuture should have stored the Exception set by the Thread that saw 'true' returned from onExceptionEvent(...)",
			        theCompletion.exception.getMessage(), e.getCause()
			                .getMessage());
		}
	}

	private static final class Completion {

		final MessageEvent	channelEvent;

		final Exception		exception;

		Completion(final MessageEvent messageEvent, final Exception exception) {
			this.channelEvent = messageEvent;
			this.exception = exception;
		}
	}

	private static final class SuccessfulCompletor implements
	        Callable<List<Completion>> {

		private final TimedFilteringChannelEventFuture<ChannelEvent>	objectUnderTest;

		private final long		                                     numberOfRounds;

		private final MessageEvent		                             matchingChannelEvent;

		private final CyclicBarrier		                             startBarrier;

		private final Random		                                 jitter	= new Random(
		                                                                            hashCode());

		SuccessfulCompletor(
		        final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest,
		        final long numberOfRounds, final CyclicBarrier startBarrier) {
			this.objectUnderTest = objectUnderTest;
			this.numberOfRounds = numberOfRounds;
			this.startBarrier = startBarrier;
			this.matchingChannelEvent = new UpstreamMessageEvent(
			        createNiceMock(Channel.class), toString(), null);
		}

		@Override
		public List<Completion> call() throws Exception {
			this.startBarrier.await();
			final List<Completion> result = new ArrayList<Completion>(1);
			Thread.sleep(this.jitter.nextInt(10));
			for (long i = 0; i < this.numberOfRounds; i++) {
				if (this.objectUnderTest
				        .acceptsChannelEvent(this.matchingChannelEvent)) {
					result.add(new Completion(this.matchingChannelEvent, null));
				}
				Thread.sleep(this.jitter.nextInt(10));
			}
			return result;
		}

		@Override
		public String toString() {
			return "SuccessfulCompletor@" + this.hashCode();
		}
	}

	private static final class FailedCompletor implements
	        Callable<List<Completion>> {

		private final TimedFilteringChannelEventFuture<ChannelEvent>	objectUnderTest;

		private final long		                                     numberOfRounds;

		private final CyclicBarrier		                             startBarrier;

		private final Exception		                                 exception;

		private final Random		                                 jitter	= new Random(
		                                                                            hashCode());

		FailedCompletor(
		        final TimedFilteringChannelEventFuture<ChannelEvent> objectUnderTest,
		        final long numberOfRounds, final CyclicBarrier startBarrier) {
			this.objectUnderTest = objectUnderTest;
			this.numberOfRounds = numberOfRounds;
			this.startBarrier = startBarrier;
			this.exception = new RuntimeException(toString());
		}

		@Override
		public List<Completion> call() throws Exception {
			this.startBarrier.await();
			final List<Completion> result = new ArrayList<Completion>(1);
			Thread.sleep(this.jitter.nextInt(10));
			for (long i = 0; i < this.numberOfRounds; i++) {
				if (this.objectUnderTest
				        .acceptsExceptionEvent(new DefaultExceptionEvent(
				                createNiceMock(Channel.class), this.exception))) {
					result.add(new Completion(null, this.exception));
				}
				Thread.sleep(this.jitter.nextInt(10));
			}
			return result;
		}

		@Override
		public String toString() {
			return "FailedCompletor@" + this.hashCode();
		}
	}
}
