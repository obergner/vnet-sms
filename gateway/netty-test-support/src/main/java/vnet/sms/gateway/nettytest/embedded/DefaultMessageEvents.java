/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.Future;

import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
class DefaultMessageEvents implements MessageEvents,
        ChannelListener<MessageEvent> {

	private final FilteredChannelEventQueue<MessageEvent>	channelEvents	= new FilteredChannelEventQueue<MessageEvent>();

	DefaultMessageEvents() {
	}

	// ------------------------------------------------------------------------
	// ChannelListener
	// ------------------------------------------------------------------------

	@Override
	public void onEvent(final MessageEvent e) {
		this.channelEvents.onEvent(e);
	}

	@Override
	public void onExceptionEvent(final ExceptionEvent e) {
		this.channelEvents.onExceptionEvent(e);
	}

	// ------------------------------------------------------------------------
	// ChannelEvents
	// ------------------------------------------------------------------------

	@Override
	public boolean isEmpty() {
		return this.channelEvents.isEmpty();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<MessageEvent> iterator() {
		return this.channelEvents.iterator();
	}

	@Override
	public Future<MessageEvent> waitForMatchingMessageEvent(
	        final Predicate<MessageEvent> predicate) {
		return this.channelEvents.addFilter(new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent input) {
				return MessageEvent.class.isInstance(input)
				        && predicate.apply(MessageEvent.class.cast(input));
			}
		});
	}

	@Override
	public TimedFuture<MessageEvent> timedWaitForMatchingMessageEvent(
	        final Predicate<MessageEvent> predicate) {
		return this.channelEvents.addTimedFilter(new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent input) {
				return MessageEvent.class.isInstance(input)
				        && predicate.apply(MessageEvent.class.cast(input));
			}
		});
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.MessageEvents#nextMessageEvent()
	 */
	@Override
	public MessageEvent nextMessageEvent() {
		return this.channelEvents.poll();
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.MessageEvents#nextMatchingMessageEvent(vnet.sms.gateway.nettytest.embedded.MessageEventFilter)
	 */
	@Override
	public MessageEvent nextMatchingMessageEvent(
	        final Predicate<MessageEvent> predicate) {
		for (final MessageEvent candidate : this.channelEvents) {
			if (predicate.apply(candidate)) {
				this.channelEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.MessageEvents#allMessageEvents()
	 */
	@Override
	public MessageEvent[] allMessageEvents() {
		final int size = this.channelEvents.size();
		final MessageEvent[] a = new MessageEvent[size];
		for (int i = 0; i < size; i++) {
			final MessageEvent product = nextMessageEvent();
			if (product == null) {
				throw new ConcurrentModificationException();
			}
			a[i] = product;
		}
		return a;
	}

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "DefaultMessageEvents@" + this.hashCode() + "[channelEvents: "
		        + this.channelEvents + "]";
	}
}
