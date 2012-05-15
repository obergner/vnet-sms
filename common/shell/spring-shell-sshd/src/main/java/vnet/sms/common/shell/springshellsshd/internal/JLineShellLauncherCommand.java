/**
 * 
 */
package vnet.sms.common.shell.springshellsshd.internal;

import static org.apache.commons.lang.Validate.notNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.shell.springshell.JLineShellComponent;
import vnet.sms.common.shell.springshell.JLineShellComponentFactory;
import vnet.sms.common.shell.springshell.event.ShellStatus;
import vnet.sms.common.shell.springshell.event.ShellStatusListener;

/**
 * @author obergner
 * 
 */
public class JLineShellLauncherCommand implements Command {

	static final class Factory implements
	        org.apache.sshd.common.Factory<Command> {

		private final JLineShellComponentFactory	jlineShellFactory;

		Factory(final JLineShellComponentFactory jlineShellFactory) {
			this.jlineShellFactory = jlineShellFactory;
		}

		@Override
		public Command create() {
			return new JLineShellLauncherCommand(this.jlineShellFactory);
		}
	}

	private final Logger	                           log	          = LoggerFactory
	                                                                          .getLogger(getClass());

	private final JLineShellComponentFactory	       jlineShellFactory;

	private final AtomicReference<JLineShellComponent>	launchedShell	= new AtomicReference<JLineShellComponent>();

	private InputStream	                               input;

	private OutputStream	                           output;

	private ExitCallback	                           exitCallback;

	/**
	 * @param jlineShellFactory
	 */
	JLineShellLauncherCommand(final JLineShellComponentFactory jlineShellFactory) {
		notNull(jlineShellFactory,
		        "Argument 'jlineShellFactory' must not be null");
		this.jlineShellFactory = jlineShellFactory;
	}

	/**
	 * @see org.apache.sshd.server.Command#setInputStream(java.io.InputStream)
	 */
	@Override
	public void setInputStream(final InputStream in) {
		this.input = in;
	}

	/**
	 * @see org.apache.sshd.server.Command#setOutputStream(java.io.OutputStream)
	 */
	@Override
	public void setOutputStream(final OutputStream out) {
		this.output = new LfToCrLfFilterOutputStream(out);
	}

	private final class LfToCrLfFilterOutputStream extends FilterOutputStream {

		private boolean	lastWasCr;

		public LfToCrLfFilterOutputStream(final OutputStream out) {
			super(out);
		}

		@Override
		public void write(final int b) throws IOException {
			if (!this.lastWasCr && (b == '\n')) {
				this.out.write('\r');
				this.out.write('\n');
			} else {
				this.out.write(b);
			}
			this.lastWasCr = b == '\r';
		}
	}

	/**
	 * @see org.apache.sshd.server.Command#setErrorStream(java.io.OutputStream)
	 */
	@Override
	public void setErrorStream(final OutputStream err) {
		// Ignored for now
	}

	/**
	 * @see org.apache.sshd.server.Command#setExitCallback(org.apache.sshd.server.ExitCallback)
	 */
	@Override
	public void setExitCallback(final ExitCallback callback) {
		this.exitCallback = callback;
	}

	/**
	 * @see org.apache.sshd.server.Command#start(org.apache.sshd.server.Environment)
	 */
	@Override
	public void start(final Environment env) throws IOException {
		this.log.info("Launching new Spring Shell ...");
		if (this.launchedShell.compareAndSet(null,
		        this.jlineShellFactory.newShell(this.input, this.output))) {
			this.launchedShell.get().addShellStatusListener(
			        this.new ShellCloseListener());
			this.launchedShell.get().start();
			this.log.info("New Spring Shell {} launched",
			        this.launchedShell.get());
		} else {
			this.log.warn(
			        "Spring Shell {} has already been launched - ignoring attempt to launch it again",
			        this.launchedShell.get());
		}

	}

	private final class ShellCloseListener implements ShellStatusListener {

		@Override
		public void onShellStatusChange(final ShellStatus oldStatus,
		        final ShellStatus newStatus) {
			if ((oldStatus.getStatus() != ShellStatus.Status.SHUTTING_DOWN)
			        && (newStatus.getStatus() == ShellStatus.Status.SHUTTING_DOWN)) {
				JLineShellLauncherCommand.this.exitCallback.onExit(0);
				JLineShellLauncherCommand.this.log
				        .info("Spring Shell has been terminated");
			}
		}
	}

	/**
	 * @see org.apache.sshd.server.Command#destroy()
	 */
	@Override
	public void destroy() {
		if (this.launchedShell.get().getShellStatus().getStatus() != ShellStatus.Status.SHUTTING_DOWN) {
			this.launchedShell.get().stop();
		}
		this.exitCallback.onExit(0);
		this.log.info("Spring Shell has been terminated");
	}
}
