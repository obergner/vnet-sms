/**
 * 
 */
package vnet.sms.common.executor.thread;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author obergner
 * 
 */
public class MessageDiagnosticContextPopulatingThreadFactory implements
        ThreadFactory {

	private String	                  threadNamePrefix;

	private int	                      threadPriority	                   = Thread.NORM_PRIORITY;

	private boolean	                  daemon	                           = false;

	private ThreadGroup	              threadGroup;

	private final Map<String, String>	messageDiagnosticContextParameters	= new HashMap<String, String>();

	private final AtomicInteger	      threadCount	                       = new AtomicInteger(
	                                                                               0);

	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(final Runnable r) {
		final Thread t = new MessageDiagnosticContextPopulatingThread(
		        this.threadGroup, r, nextThreadName(),
		        this.messageDiagnosticContextParameters);
		t.setDaemon(this.daemon);
		t.setPriority(this.threadPriority);
		return t;
	}

	private String nextThreadName() {
		return getMandatoryThreadNamePrefix() + " #"
		        + this.threadCount.getAndIncrement();
	}

	/**
	 * @param threadNamePrefix
	 *            the threadNamePrefix to set
	 */
	public final void setThreadNamePrefix(final String threadNamePrefix) {
		notEmpty(threadNamePrefix,
		        "Argument 'threadNamePrefix' must be neither null nor empty");
		this.threadNamePrefix = threadNamePrefix;
	}

	private String getMandatoryThreadNamePrefix() {
		if (this.threadNamePrefix == null) {
			throw new IllegalStateException("No threadNamePrefix has been set");
		}
		return this.threadNamePrefix;
	}

	/**
	 * @param threadPriority
	 *            the threadPriority to set
	 */
	public final void setThreadPriority(final int threadPriority) {
		this.threadPriority = threadPriority;
	}

	/**
	 * @param daemon
	 *            the daemon to set
	 */
	public final void setDaemon(final boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * @param threadGroup
	 *            the threadGroup to set
	 */
	public final void setThreadGroup(final ThreadGroup threadGroup) {
		notNull(threadGroup, "Argument 'threadGroup' must not be null");
		this.threadGroup = threadGroup;
	}

	public final void setThreadGroupName(final String threadGroupName) {
		notEmpty(threadGroupName,
		        "Argument 'threadGroupName' must not be neither null nor empty. Got: "
		                + threadGroupName);
		setThreadGroup(new ThreadGroup(threadGroupName));
	}

	/**
	 * @param messageDiagnosticContextParameters
	 *            the messageDiagnosticContextParameters to set
	 */
	public final void setMessageDiagnosticContextParameters(
	        final Map<String, String> messageDiagnosticContextParameters) {
		notNull(messageDiagnosticContextParameters,
		        "Argument 'messageDiagnosticContextParameters' must not be null");
		this.messageDiagnosticContextParameters
		        .putAll(messageDiagnosticContextParameters);
	}
}
