package vnet.sms.gateway.nettytest.embedded;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import com.google.common.base.Predicate;

public class DefaultMessageEventsTest {

	@Test
	public final void assertThatOnChannelEventStoresSuppliedMessageEventForLaterRetrieval() {
		final MessageEvent suppliedMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();

		objectUnderTest.onEvent(suppliedMessageEvent);

		final MessageEvent storedMessageEvent = objectUnderTest
		        .nextMessageEvent();

		assertEquals(
		        "onChannelEvent(...) should have stored supplied channel event for later retrieval",
		        suppliedMessageEvent, storedMessageEvent);
	}

	@Test
	public final void assertThatOnExceptionEventFailsPreviouslyRegisteredFilteringMessageEventFuture() {
		final ExceptionEvent e = new DefaultExceptionEvent(
		        createNiceMock(Channel.class),
		        new RuntimeException(
		                "assertThatOnExceptionEventFailsPreviouslyRegisteredFilteringMessageEventFuture"));
		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();

		final Future<? extends MessageEvent> future = objectUnderTest
		        .waitForMatchingMessageEvent(MessageEventFilters
		                .ofType(MessageEvent.class));
		objectUnderTest.onExceptionEvent(e);

		assertTrue(
		        "onExceptionEvent(...) should have failed any previously registered Futures that have not been completed yet",
		        future.isDone());
	}

	@Test
	public final void assertThatIsEmptyInitiallyReturnsTrue() {
		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();

		assertTrue(
		        "isEmpty() should return true immediately after creating new DefaultMessageEvents instance",
		        objectUnderTest.isEmpty());
	}

	@Test
	public final void assertThatIteratorContainsPreviouslyAddedMessageEvent() {
		final MessageEvent suppliedMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(suppliedMessageEvent);

		final MessageEvent retrievedMessageEvent = objectUnderTest.iterator()
		        .next();

		assertEquals(
		        "Iterator returned from iterator() should contain previously added MessageEvent",
		        suppliedMessageEvent, retrievedMessageEvent);
	}

	@Test
	public final void assertThatNextMessageEventReturnsFirstMessageEventInQueue() {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		final MessageEvent nextMessageEvent = objectUnderTest
		        .nextMessageEvent();

		assertSame(
		        "nextMessageEvent() should have returned the first MessageEvent currently stored",
		        firstMessageEvent, nextMessageEvent);
	}

	@Test
	public final void assertThatCallingNextMessageEventTwiceReturnsSecondMessageEventInQueue() {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		objectUnderTest.nextMessageEvent(); // Not interested in return value
		final MessageEvent nextMessageEvent = objectUnderTest
		        .nextMessageEvent();

		assertSame(
		        "Calling nextMessageEvent() for the second time should have returned the second MessageEvent in queue",
		        secondMessageEvent, nextMessageEvent);
	}

	@Test
	public final void assertThatNextMatchingMessageEventReturnsTheFirstMatchingMessageEvent() {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final Predicate<MessageEvent> matchSecondEvent = new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent input) {
				return input == secondMessageEvent;
			}
		};

		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		final MessageEvent matchingMessageEvent = objectUnderTest
		        .nextMatchingMessageEvent(matchSecondEvent);

		assertSame(
		        "nextMatchingMessageEvent(...) should have returned the first matching MessageEvent",
		        secondMessageEvent, matchingMessageEvent);
	}

	@Test
	public final void assertThatNextMatchingMessageEventRemovesMatchFromQueue() {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final Predicate<MessageEvent> matchSecondEvent = new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent input) {
				return input == secondMessageEvent;
			}
		};

		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		objectUnderTest.nextMatchingMessageEvent(matchSecondEvent);
		final MessageEvent secondMatchingMessageEvent = objectUnderTest
		        .nextMatchingMessageEvent(matchSecondEvent);

		assertNull(
		        "nextMatchingMessageEvent(...) should have removed the matching MessageEvent from queue",
		        secondMatchingMessageEvent);
	}

	@Test
	public final void assertThatAllMessageEventsReturnsAllStoredMessageEventsInTheOrderTheyArrived() {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		final MessageEvent[] allMessageEvents = objectUnderTest
		        .allMessageEvents();

		assertSame(
		        "Array returned from allMessageEvents() should contain MessageEvent first added to queue as its first element",
		        firstMessageEvent, allMessageEvents[0]);
		assertSame(
		        "Array returned from allMessageEvents() should contain MessageEvent added to queue after first element as its second element",
		        secondMessageEvent, allMessageEvents[1]);
	}

	@Test
	public final void assertThatWaitForMatchingMessageEventReturnsFirstMatchingMessageEventFuture()
	        throws InterruptedException, ExecutionException {
		final MessageEvent firstMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final MessageEvent secondMessageEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);

		final Predicate<MessageEvent> matchSecondEvent = new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent input) {
				return input == secondMessageEvent;
			}
		};
		final DefaultMessageEvents objectUnderTest = new DefaultMessageEvents();
		final Future<MessageEvent> matchingMessageEvent = objectUnderTest
		        .waitForMatchingMessageEvent(matchSecondEvent);

		objectUnderTest.onEvent(firstMessageEvent);
		objectUnderTest.onEvent(secondMessageEvent);

		assertSame(
		        "waitForMatchingMessageEvent(...) should have returned the first matching MessageEvent, wrapped in a Future",
		        secondMessageEvent, matchingMessageEvent.get());
	}
}
