/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author obergner
 * 
 */
public interface TimedFuture<T> {

	public interface Value<T> {

		long elapsedDurationMillis();

		T get() throws ExecutionException;
	}

	boolean cancel(boolean mayInterruptIfRunning);

	boolean isCancelled();

	boolean isDone();

	Value<T> get() throws InterruptedException, ExecutionException;

	Value<T> get(long timeout, TimeUnit unit) throws InterruptedException,
	        ExecutionException, TimeoutException;
}
