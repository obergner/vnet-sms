/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ExceptionEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
class FilteredChannelEventQueue<T extends ChannelEvent> implements
        ChannelListener<T>, Queue<T> {

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	private final Queue<T>	                channelEvents;

	private final List<ChannelEventSink<T>>	filters	= new CopyOnWriteArrayList<ChannelEventSink<T>>();

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	FilteredChannelEventQueue() {
		this(new LinkedList<T>());
	}

	/**
	 * EXPOSED FOR TESTING PURPOSES.
	 */
	FilteredChannelEventQueue(final Queue<T> channelEvents) {
		this.channelEvents = channelEvents;
	}

	// ------------------------------------------------------------------------
	// Adding filters
	// ------------------------------------------------------------------------

	FilteringChannelEventFuture<T> addFilter(final Predicate<T> filter) {
		final FilteringChannelEventFuture<T> filterFuture = new FilteringChannelEventFuture<T>(
		        filter);
		for (final T e : this.channelEvents) {
			if (filterFuture.acceptsChannelEvent(e)) {
				this.channelEvents.remove(e);
				return filterFuture;
			}
		}
		this.filters.add(filterFuture);
		return filterFuture;
	}

	TimedFilteringChannelEventFuture<T> addTimedFilter(final Predicate<T> filter) {
		final TimedFilteringChannelEventFuture<T> filterFuture = new TimedFilteringChannelEventFuture<T>(
		        filter);
		for (final T e : this.channelEvents) {
			if (filterFuture.acceptsChannelEvent(e)) {
				this.channelEvents.remove(e);
				return filterFuture;
			}
		}
		this.filters.add(filterFuture);
		return filterFuture;
	}

	// ------------------------------------------------------------------------
	// ChannelListener
	// ------------------------------------------------------------------------

	@Override
	public void onEvent(final T e) {
		for (final ChannelEventSink<T> filterFuture : this.filters) {
			if (filterFuture.acceptsChannelEvent(e)) {
				this.channelEvents.remove(e);
				this.filters.remove(filterFuture);
				break;
			}
		}
		this.channelEvents.add(e);
	}

	@Override
	public void onExceptionEvent(final ExceptionEvent e) {
		for (final ChannelEventSink<T> filterFuture : this.filters) {
			if (filterFuture.acceptsExceptionEvent(e)) {
				this.filters.remove(filterFuture);
			}
		}
	}

	// ------------------------------------------------------------------------
	// Queue
	// ------------------------------------------------------------------------

	/**
	 * @param e
	 * @return
	 * @see java.util.Queue#add(java.lang.Object)
	 */
	@Override
	public boolean add(final T e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object o) {
		return this.channelEvents.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.channelEvents.containsAll(c);
	}

	/**
	 * @return
	 * @see java.util.Queue#element()
	 */
	@Override
	public T element() {
		return this.channelEvents.element();
	}

	/**
	 * @return
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.channelEvents.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return this.channelEvents.iterator();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(final ChannelEvent e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return
	 * @see java.util.Queue#peek()
	 */
	@Override
	public T peek() {
		return this.channelEvents.peek();
	}

	/**
	 * @return
	 * @see java.util.Queue#poll()
	 */
	@Override
	public T poll() {
		return this.channelEvents.poll();
	}

	/**
	 * @return
	 * @see java.util.Queue#remove()
	 */
	@Override
	public T remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {
		return this.channelEvents.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return this.channelEvents.size();
	}

	/**
	 * @return
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return this.channelEvents.toArray();
	}

	/**
	 * @param <S>
	 * @param a
	 * @return
	 * @see java.util.Collection#toArray(S[])
	 */
	@Override
	public <S> S[] toArray(final S[] a) {
		return this.channelEvents.toArray(a);
	}
}
