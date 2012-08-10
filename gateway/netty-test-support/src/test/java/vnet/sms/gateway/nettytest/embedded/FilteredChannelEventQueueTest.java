package vnet.sms.gateway.nettytest.embedded;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import com.google.common.base.Predicate;

public class FilteredChannelEventQueueTest {

	@Test
	public final void assertThatAddFilterReturnsDoneFutureIfFilteredChannelEventQueueContainsAMatchingChannelEvent() {
		final MessageEvent matchingEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest(matchingEvent);

		final FilteringChannelEventFuture<ChannelEvent> channelEventFuture = objectUnderTest
		        .addFilter(new Predicate<ChannelEvent>() {
			        @Override
			        public boolean apply(final ChannelEvent input) {
				        return input == matchingEvent;
			        }
		        });

		assertTrue(
		        "addFilter(...) should have returned a done Future since the FilteredChannelEventQueue that Predicate has been "
		                + "added to contains a matching ChannelEvent",
		        channelEventFuture.isDone());
	}

	private FilteredChannelEventQueue<ChannelEvent> newObjectUnderTest(
	        final MessageEvent... initialEvents) {
		final Queue<ChannelEvent> channelEvents = new LinkedList<ChannelEvent>();
		for (final MessageEvent event : initialEvents) {
			channelEvents.add(event);
		}
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = new FilteredChannelEventQueue<ChannelEvent>(
		        channelEvents);
		return objectUnderTest;
	}

	@Test
	public final void assertThatAddFilterReturnsIncompleteFutureIfFilteredChannelEventQueueDoesNOTContainAMatchingChannelEvent() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();

		final FilteringChannelEventFuture<ChannelEvent> channelEventFuture = objectUnderTest
		        .addFilter(new Predicate<ChannelEvent>() {
			        @Override
			        public boolean apply(final ChannelEvent input) {
				        return false;
			        }
		        });

		assertFalse(
		        "addFilter(...) should have returned an incomplete Future since the FilteredChannelEventQueue that Predicate has been "
		                + "added to does NOT contain a matching ChannelEvent",
		        channelEventFuture.isDone());
	}

	@Test
	public final void assertThatOnChannelEventAddsChannelEventToUnderlyingQueue() {
		final MessageEvent matchingEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();

		objectUnderTest.onEvent(matchingEvent);

		assertTrue(
		        "onChannelEvent(matchingEvent) should have added supplied ChannelEvent to underlying Queue",
		        objectUnderTest.contains(matchingEvent));
	}

	@Test
	public final void assertThatOnChannelEventCompletesAMatchingFilteredChannelEventFuture() {
		final MessageEvent matchingEvent = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		final Predicate<ChannelEvent> matchingFilter = new Predicate<ChannelEvent>() {
			@Override
			public boolean apply(final ChannelEvent input) {
				return input == matchingEvent;
			}
		};
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final FilteringChannelEventFuture<ChannelEvent> channelEventFuture = objectUnderTest
		        .addFilter(matchingFilter);

		objectUnderTest.onEvent(matchingEvent);

		assertTrue(
		        "onChannelEvent(matchingEvent) should have completed a previously added FilteringChannelEventFuture that matches the supplied ChannelEvent",
		        channelEventFuture.isDone());
	}

	@Test
	public final void assertThatOnExceptionEventCompletesAllRegisteredFilteringChannelEventFutures() {
		final int noFilters = 10;
		final List<FilteringChannelEventFuture<ChannelEvent>> filteringFutures = new ArrayList<FilteringChannelEventFuture<ChannelEvent>>(
		        noFilters);

		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		for (int i = 0; i < noFilters; i++) {
			final FilteringChannelEventFuture<ChannelEvent> channelEventFuture = objectUnderTest
			        .addFilter(new Predicate<ChannelEvent>() {
				        @Override
				        public boolean apply(final ChannelEvent input) {
					        return false;
				        }
			        });
			filteringFutures.add(channelEventFuture);
		}

		final Exception error = new RuntimeException();
		final ExceptionEvent exceptionEvent = new DefaultExceptionEvent(
		        createNiceMock(Channel.class), error);
		objectUnderTest.onExceptionEvent(exceptionEvent);

		for (final FilteringChannelEventFuture<ChannelEvent> future : filteringFutures) {
			assertTrue(
			        "onExceptionEvent(exceptionEvent) should have completed all registered FilteringChannelEventFutures",
			        future.isDone());
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatAddThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.add(e);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatAddAllThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.addAll(Collections.singleton(e));
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatClearThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		objectUnderTest.clear();

	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatOfferThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.offer(e);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatRemoveThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		objectUnderTest.remove();
	}

	@Test
	public final void assertThatRemoveObjectDoesRemoveSuppliedChannelEventFromQueue() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.remove(e);

		assertFalse(
		        "remove(e) should have removed e from FilteredChannelEventQueue",
		        objectUnderTest.contains(e));
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatRemoveAllThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.removeAll(Collections.singleton(e));
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatRetainAllThrowsUnsupportedOperationException() {
		final FilteredChannelEventQueue<ChannelEvent> objectUnderTest = newObjectUnderTest();
		final UpstreamMessageEvent e = new UpstreamMessageEvent(
		        createNiceMock(Channel.class), new Object(), null);
		objectUnderTest.retainAll(Collections.singleton(e));
	}
}
