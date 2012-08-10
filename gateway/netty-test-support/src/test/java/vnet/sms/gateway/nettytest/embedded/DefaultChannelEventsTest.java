package vnet.sms.gateway.nettytest.embedded;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.junit.Test;

import com.google.common.base.Predicate;

public class DefaultChannelEventsTest {

	@Test
	public final void assertThatOnChannelEventStoresSuppliedChannelEventForLaterRetrieval() {
		final ChannelEvent suppliedChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();

		objectUnderTest.onEvent(suppliedChannelEvent);

		final ChannelEvent storedChannelEvent = objectUnderTest
		        .nextChannelEvent();

		assertEquals(
		        "onChannelEvent(...) should have stored supplied channel event for later retrieval",
		        suppliedChannelEvent, storedChannelEvent);
	}

	@Test
	public final void assertThatIsEmptyInitiallyReturnsTrue() {
		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();

		assertTrue(
		        "isEmpty() should return true immediately after creating new DefaultChannelEvents instance",
		        objectUnderTest.isEmpty());
	}

	@Test
	public final void assertThatIteratorContainsPreviouslyAddedChannelEvent() {
		final ChannelEvent suppliedChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(suppliedChannelEvent);

		final ChannelEvent retrievedChannelEvent = objectUnderTest.iterator()
		        .next();

		assertEquals(
		        "Iterator returned from iterator() should contain previously added ChannelEvent",
		        suppliedChannelEvent, retrievedChannelEvent);
	}

	@Test
	public final void assertThatNextChannelEventReturnsFirstChannelEventInQueue() {
		final ChannelEvent firstChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final ChannelEvent secondChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);

		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(firstChannelEvent);
		objectUnderTest.onEvent(secondChannelEvent);

		final ChannelEvent nextChannelEvent = objectUnderTest
		        .nextChannelEvent();

		assertSame(
		        "nextChannelEvent() should have returned the first ChannelEvent currently stored",
		        firstChannelEvent, nextChannelEvent);
	}

	@Test
	public final void assertThatCallingNextChannelEventTwiceReturnsSecondChannelEventInQueue() {
		final ChannelEvent firstChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final ChannelEvent secondChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);

		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(firstChannelEvent);
		objectUnderTest.onEvent(secondChannelEvent);

		objectUnderTest.nextChannelEvent(); // Not interested in return value
		final ChannelEvent nextChannelEvent = objectUnderTest
		        .nextChannelEvent();

		assertSame(
		        "Calling nextChannelEvent() for the second time should have returned the second ChannelEvent in queue",
		        secondChannelEvent, nextChannelEvent);
	}

	@Test
	public final void assertThatNextMatchingChannelEventReturnsTheFirstMatchingChannelEvent() {
		final ChannelEvent firstChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final ChannelEvent secondChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);

		final Predicate<ChannelEvent> matchSecondEvent = new Predicate<ChannelEvent>() {
			@Override
			public boolean apply(final ChannelEvent input) {
				return input == secondChannelEvent;
			}
		};

		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(firstChannelEvent);
		objectUnderTest.onEvent(secondChannelEvent);

		final ChannelEvent matchingChannelEvent = objectUnderTest
		        .nextMatchingChannelEvent(matchSecondEvent);

		assertSame(
		        "nextMatchingChannelEvent(...) should have returned the first matching ChannelEvent",
		        secondChannelEvent, matchingChannelEvent);
	}

	@Test
	public final void assertThatNextMatchingChannelEventRemovesMatchFromQueue() {
		final ChannelEvent firstChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final ChannelEvent secondChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);

		final Predicate<ChannelEvent> matchSecondEvent = new Predicate<ChannelEvent>() {
			@Override
			public boolean apply(final ChannelEvent input) {
				return input == secondChannelEvent;
			}
		};

		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(firstChannelEvent);
		objectUnderTest.onEvent(secondChannelEvent);

		objectUnderTest.nextMatchingChannelEvent(matchSecondEvent);
		final ChannelEvent secondMatchingChannelEvent = objectUnderTest
		        .nextMatchingChannelEvent(matchSecondEvent);

		assertNull(
		        "nextMatchingChannelEvent(...) should have removed the matching ChannelEvent from queue",
		        secondMatchingChannelEvent);
	}

	@Test
	public final void assertThatAllChannelEventsReturnsAllStoredChannelEventsInTheOrderTheyArrived() {
		final ChannelEvent firstChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);
		final ChannelEvent secondChannelEvent = new UpstreamChannelStateEvent(
		        createNiceMock(Channel.class), ChannelState.BOUND, null);

		final DefaultChannelEvents objectUnderTest = new DefaultChannelEvents();
		objectUnderTest.onEvent(firstChannelEvent);
		objectUnderTest.onEvent(secondChannelEvent);

		final ChannelEvent[] allChannelEvents = objectUnderTest
		        .allChannelEvents();

		assertSame(
		        "Array returned from allChannelEvents() should contain ChannelEvent first added to queue as its first element",
		        firstChannelEvent, allChannelEvents[0]);
		assertSame(
		        "Array returned from allChannelEvents() should contain ChannelEvent added to queue after first element as its second element",
		        secondChannelEvent, allChannelEvents[1]);
	}
}
