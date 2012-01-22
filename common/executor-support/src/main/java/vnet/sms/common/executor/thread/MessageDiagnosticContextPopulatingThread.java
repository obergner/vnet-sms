package vnet.sms.common.executor.thread;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

final class MessageDiagnosticContextPopulatingThread extends Thread {

	private final Map<String, String>	mdcParameters	= new HashMap<String, String>();

	MessageDiagnosticContextPopulatingThread(final ThreadGroup threadGroup,
	        final Runnable runnable, final String threadName,
	        final Map<String, String> mdcParameters) {
		super(threadGroup, runnable, threadName);
		this.mdcParameters.putAll(mdcParameters);
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			for (final Map.Entry<String, String> mdcParam : this.mdcParameters
			        .entrySet()) {
				MDC.put(mdcParam.getKey(), mdcParam.getValue());
			}
			super.run();
		} finally {
			for (final String mdcKey : this.mdcParameters.keySet()) {
				MDC.remove(mdcKey);
			}
		}
	}
}
