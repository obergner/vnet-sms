/**
 * 
 */
package vnet.sms.common.shell.clamshellsshsrv;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author obergner
 * 
 */
public final class Defaults {

	public static final String	                 DEFAULT_HOST	                       = "localhost";

	public static final int	                     DEFAULT_PORT	                       = 2222;

	public static final String	                 DEFAULT_HOST_KEY_PATH	               = "etc/hostkey.ser";

	public static final ScheduledExecutorService	DEFAULT_SCHEDULED_EXECUTOR_SERVICE	= Executors
	                                                                                           .newSingleThreadScheduledExecutor();

	private Defaults() {
		// Noop
	}
}
