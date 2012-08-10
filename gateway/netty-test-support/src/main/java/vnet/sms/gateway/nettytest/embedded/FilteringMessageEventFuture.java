package vnet.sms.gateway.nettytest.embedded;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;

import com.google.common.base.Predicate;

class FilteringMessageEventFuture implements Future<MessageEvent> {

	private final Predicate<MessageEvent>	filter;

	private final CountDownLatch	      done	  = new CountDownLatch(1);

	private final AtomicReference<Value>	value	= new AtomicReference<Value>();

	FilteringMessageEventFuture(final Predicate<MessageEvent> filter) {
		this.filter = filter;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		// We cannot be canceled
		return false;
	}

	@Override
	public MessageEvent get() throws InterruptedException, ExecutionException {
		this.done.await();
		return this.value.get().resolve();
	}

	@Override
	public MessageEvent get(final long timeout, final TimeUnit unit)
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

	boolean acceptsMessageEvent(final MessageEvent candidate) {
		if (this.filter.apply(candidate)
		        && this.value.compareAndSet(null,
		                Value.fromMessageEvent(candidate))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	boolean acceptsExceptionEvent(final ExceptionEvent e) {
		if (this.value.compareAndSet(null, Value.fromException(e.getCause()))) {
			this.done.countDown();
			return true;
		}
		return false;
	}

	private static final class Value {

		static Value fromMessageEvent(final MessageEvent messageEvent) {
			return new Value(messageEvent, null);
		}

		static Value fromException(final Throwable exception) {
			return new Value(null, exception);
		}

		private final MessageEvent	messageEvent;

		private final Throwable		exception;

		private Value(final MessageEvent messageEvent, final Throwable exception) {
			isTrue(((messageEvent != null) && (exception == null))
			        || ((messageEvent == null) && (exception != null)),
			        "Exactly one of 'channelEvent' and 'exception' must be non-null");
			this.messageEvent = messageEvent;
			this.exception = exception;
		}

		MessageEvent resolve() throws ExecutionException {
			if (this.exception != null) {
				throw new ExecutionException(this.exception);
			}
			return this.messageEvent;
		}
	}
}
