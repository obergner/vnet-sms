/**
 * 
 */
package vnet.sms.common.shell.clamshellsshsrv.internal;

import static org.apache.commons.lang.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;

/**
 * @author obergner
 * 
 */
public class ClamshellLauncherCommand implements Command {

	static final class Factory implements
	        org.apache.sshd.common.Factory<Command> {

		private final ClamshellLauncher.Factory	clamshellLauncherFactory;

		Factory(final ClamshellLauncher.Factory clamshellLauncherFactory) {
			this.clamshellLauncherFactory = clamshellLauncherFactory;
		}

		@Override
		public Command create() {
			return new ClamshellLauncherCommand(
			        this.clamshellLauncherFactory.newLauncher());
		}
	}

	private final Logger	        log	= LoggerFactory.getLogger(getClass());

	private final ClamshellLauncher	clamshellLauncher;

	private InputStream	            input;

	private OutputStream	        output;

	private ExitCallback	        exitCallback;

	/**
	 * @param clamshellLauncher
	 */
	ClamshellLauncherCommand(final ClamshellLauncher clamshellLauncher) {
		notNull(clamshellLauncher,
		        "Argument 'clamshellLauncher' must not be null");
		this.clamshellLauncher = clamshellLauncher;
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
		this.output = out;
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
		this.log.info("Launching new Clamshell ...");
		this.clamshellLauncher.launch(this.input, this.output);
		this.log.info("New Clamshell launched");
	}

	/**
	 * @see org.apache.sshd.server.Command#destroy()
	 */
	@Override
	public void destroy() {
		this.exitCallback.onExit(0);
		this.log.info("Clamshell has been terminated");
	}
}
