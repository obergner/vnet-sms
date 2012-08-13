package vnet.sms.gateway.nettytest.embedded;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ExceptionEvent;

import com.google.common.base.Predicate;

class FilteringChannelEventFuture<T extends ChannelEvent> implements Future<T>,
        ChannelEventSink<T> {

	private final Predicate<T>	            filter;

	private final CountDownLatch	        done	= new CountDownLatch(1);

	private final AtomicReference<Value<T>>	value	= new AtomicReference<Value<T>>();

	FilteringChannelEventFuture(final Predicate<T> filter) {
		this.filter = filter;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		// We cannot be canceled
		return false;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		this.done.await();
		return this.value.get().resolve();
	}

	@Override
	public T get(final long timeout, final TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException {
		if (!this.done.await(timeout, unit)) {
			throw new TimeoutException("Timed get timed out after timeout of ["
			        + timeout + "] " + unit);
		}
		return this.value.get().resolve();
	}

	@Override
	public boolean isCancelled() {
		// We cannot be canceled
		return false;
	}

	@Override
	public boolean isDone() {
		return this.done.getCount() == 0;
	}

	@Override
	public boolean acceptsChannelEvent(final T candidate) {
		if (this.filter.apply(candidate)
		        && this.value.compareAndSet(null,
		                Value.fromChannelEvent(candidate))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptsExceptionEvent(final ExceptionEvent e) {
		if (this.value.compareAndSet(null,
		        Value.<T> fromException(e.getCause()))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	private static final class Value<T extends ChannelEvent> {

		static <T extends ChannelEvent> Value<T> fromChannelEvent(
		        final T channelEvent) {
			return new Value<T>(channelEvent, null);
		}

		static <T extends ChannelEvent> Value<T> fromException(
		        final Throwable exception) {
			return new Value<T>(null, exception);
		}

		private final T		    channelEvent;

		private final Throwable	exception;

		private Value(final T channelEvent, final Throwable exception) {
			isTrue(((channelEvent != null) && (exception == null))
			        || ((channelEvent == null) && (exception != null)),
			        "Exactly one of 'channelEvent' and 'exception' must be non-null");
			this.channelEvent = channelEvent;
			this.exception = exception;
		}

		T resolve() throws ExecutionException {
			if (this.exception != null) {
				throw new ExecutionException(this.exception);
			}
			return this.channelEvent;
		}
	}
}
