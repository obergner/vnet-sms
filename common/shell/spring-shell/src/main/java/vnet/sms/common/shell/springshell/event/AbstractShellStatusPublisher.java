package vnet.sms.common.shell.springshell.event;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import vnet.sms.common.shell.springshell.ParseResult;
import vnet.sms.common.shell.springshell.event.ShellStatus.Status;
import vnet.sms.common.shell.springshell.internal.util.Assert;

/**
 * Provides a convenience superclass for those shells wishing to publish status
 * messages.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public abstract class AbstractShellStatusPublisher implements
        ShellStatusProvider {

	// Fields
	protected Set<ShellStatusListener>	shellStatusListeners	= new CopyOnWriteArraySet<ShellStatusListener>();

	protected ShellStatus	           shellStatus	         = new ShellStatus(
	                                                                 Status.STARTING);

	@Override
	public final void addShellStatusListener(
	        final ShellStatusListener shellStatusListener) {
		Assert.notNull(shellStatusListener, "Status listener required");
		synchronized (this) {
			this.shellStatusListeners.add(shellStatusListener);
		}
	}

	@Override
	public final void removeShellStatusListener(
	        final ShellStatusListener shellStatusListener) {
		Assert.notNull(shellStatusListener, "Status listener required");
		synchronized (this) {
			this.shellStatusListeners.remove(shellStatusListener);
		}
	}

	@Override
	public final ShellStatus getShellStatus() {
		synchronized (this) {
			return this.shellStatus;
		}
	}

	protected void setShellStatus(final Status shellStatus) {
		setShellStatus(shellStatus, null, null);
	}

	protected void setShellStatus(final Status shellStatus, final String msg,
	        final ParseResult parseResult) {
		Assert.notNull(shellStatus, "Shell status required");

		synchronized (this) {
			ShellStatus st;
			if ((msg == null) || (msg.length() == 0)) {
				st = new ShellStatus(shellStatus);
			} else {
				st = new ShellStatus(shellStatus, msg, parseResult);
			}

			if (this.shellStatus.equals(st)) {
				return;
			}

			for (final ShellStatusListener listener : this.shellStatusListeners) {
				listener.onShellStatusChange(this.shellStatus, st);
			}
			this.shellStatus = st;
		}
	}
}
