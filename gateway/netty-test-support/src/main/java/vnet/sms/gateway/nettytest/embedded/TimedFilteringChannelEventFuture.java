package vnet.sms.gateway.nettytest.embedded;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ExceptionEvent;

import com.google.common.base.Predicate;

class TimedFilteringChannelEventFuture<T extends ChannelEvent> implements
        TimedFuture<T>, ChannelEventSink<T> {

	private final Predicate<T>	                 filter;

	private final CountDownLatch	             done	       = new CountDownLatch(
	                                                                   1);

	private final AtomicReference<TimedValue<T>>	timedValue	= new AtomicReference<TimedValue<T>>();

	private final long	                         start	       = System.currentTimeMillis();

	TimedFilteringChannelEventFuture(final Predicate<T> filter) {
		this.filter = filter;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		// We cannot be canceled
		return false;
	}

	@Override
	public TimedFuture.Value<T> get() throws InterruptedException,
	        ExecutionException {
		this.done.await();
		return this.timedValue.get();
	}

	@Override
	public TimedFuture.Value<T> get(final long timeout, final TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException {
		if (!this.done.await(timeout, unit)) {
			throw new TimeoutException("Timed get timed out after timeout of ["
			        + timeout + "] " + unit);
		}
		return this.timedValue.get();
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
		        && this.timedValue.compareAndSet(null,
		                TimedValue.<T> fromChannelEvent(candidate, this.start))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptsExceptionEvent(final ExceptionEvent e) {
		if (this.timedValue.compareAndSet(null,
		        TimedValue.<T> fromException(e.getCause(), this.start))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	private static final class TimedValue<T extends ChannelEvent> implements
	        TimedFuture.Value<T> {

		static <T extends ChannelEvent> TimedValue<T> fromChannelEvent(
		        final T channelEvent, final long start) {
			return new TimedValue<T>(channelEvent, null, start);
		}

		static <T extends ChannelEvent> TimedValue<T> fromException(
		        final Throwable exception, final long start) {
			return new TimedValue<T>(null, exception, start);
		}

		private final T		    channelEvent;

		private final Throwable	exception;

		private final long		start;

		private final long		end;

		private TimedValue(final T channelEvent, final Throwable exception,
		        final long start) {
			isTrue(((channelEvent != null) && (exception == null))
			        || ((channelEvent == null) && (exception != null)),
			        "Exactly one of 'channelEvent' and 'exception' must be non-null");
			this.channelEvent = channelEvent;
			this.exception = exception;
			this.start = start;
			this.end = System.currentTimeMillis();
		}

		@Override
		public T get() throws ExecutionException {
			if (this.exception != null) {
				throw new ExecutionException(this.exception);
			}
			return this.channelEvent;
		}

		@Override
		public long elapsedDurationMillis() {
			return this.end - this.start;
		}

		@Override
		public String toString() {
			return "TimedValue@" + this.hashCode() + "[channelEvent: "
			        + this.channelEvent + "|exception: " + this.exception
			        + "|start: " + this.start + "|end: " + this.end + "]";
		}
	}
}
