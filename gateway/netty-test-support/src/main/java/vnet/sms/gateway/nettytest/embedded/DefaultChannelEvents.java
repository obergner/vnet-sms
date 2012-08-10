/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ExceptionEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
class DefaultChannelEvents implements ChannelEvents,
        ChannelListener<ChannelEvent> {

	private final FilteredChannelEventQueue<ChannelEvent>	channelEvents	= new FilteredChannelEventQueue<ChannelEvent>();

	DefaultChannelEvents() {
	}

	// ------------------------------------------------------------------------
	// ChannelListener
	// ------------------------------------------------------------------------

	@Override
	public void onEvent(final ChannelEvent e) {
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
	public Iterator<ChannelEvent> iterator() {
		return this.channelEvents.iterator();
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.ChannelEvents#nextChannelEvent()
	 */
	@Override
	public ChannelEvent nextChannelEvent() {
		return this.channelEvents.poll();
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.ChannelEvents#nextMatchingChannelEvent(vnet.sms.gateway.nettytest.embedded.ChannelEventFilter)
	 */
	@Override
	public ChannelEvent nextMatchingChannelEvent(
	        final Predicate<ChannelEvent> predicate) {
		for (final ChannelEvent candidate : this.channelEvents) {
			if (predicate.apply(candidate)) {
				this.channelEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	/**
	 * @see vnet.sms.gateway.nettytest.embedded.ChannelEvents#allChannelEvents()
	 */
	@Override
	public ChannelEvent[] allChannelEvents() {
		final int size = this.channelEvents.size();
		final ChannelEvent[] a = new ChannelEvent[size];
		for (int i = 0; i < size; i++) {
			final ChannelEvent product = nextChannelEvent();
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
		return "DefaultChannelEvents@" + this.hashCode() + "[channelEvents: "
		        + this.channelEvents + "]";
	}
}
