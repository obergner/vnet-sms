/**
 * 
 */
package vnet.sms.common.shell.clamshellsshsrv.internal;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.security.PublicKey;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;
import vnet.sms.common.shell.clamshellspring.ClamshellLauncherFactoryAware;
import vnet.sms.common.shell.clamshellsshsrv.Defaults;

/**
 * @author obergner
 * 
 */
public class SshServerFactoryBean implements FactoryBean<SshServer>,
        ClamshellLauncherFactoryAware, InitializingBean, DisposableBean {

	private final Logger	         log	                     = LoggerFactory
	                                                                     .getLogger(getClass());

	private String	                 host	                     = Defaults.DEFAULT_HOST;

	private int	                     port	                     = Defaults.DEFAULT_PORT;

	private String	                 hostKeyPath	             = Defaults.DEFAULT_HOST_KEY_PATH;

	private boolean	                 autostart	                 = false;

	private Factory<Command>	     shellFactory;

	private ScheduledExecutorService	scheduledExecutorService	= Defaults.DEFAULT_SCHEDULED_EXECUTOR_SERVICE;

	private SshServer	             product;

	@Override
	public SshServer getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No SshServer instance has been created yet. Did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		}
		return this.product;
	}

	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass() : SshServer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		this.log.info("About to stop SshServer {} ...", this.product);

		final long before = System.currentTimeMillis();
		this.product.stop();

		this.log.info("Stopped SshServer {} in {} ms", this.product,
		        System.currentTimeMillis() - before);
		this.product = null;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.log.info("About to create new SshServer instance ...");
		checkConfig();

		final SshServer newSshServer = SshServer.setUpDefaultServer();

		newSshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(final String username,
			        final String password, final ServerSession session) {
				return (username != null) && username.equals(password);
			}
		});
		this.log.debug(
		        "Set default password authenticator on new SshServer instance {} - "
		                + "this password authenticator will accept all login attempts where username equals password",
		        newSshServer);

		newSshServer.setPublickeyAuthenticator(new PublickeyAuthenticator() {
			@Override
			public boolean authenticate(final String username,
			        final PublicKey key, final ServerSession session) {
				return true;
			}
		});
		this.log.debug(
		        "Set default public key authenticator on new SshServer instance {} - "
		                + "this password authenticator will accept all public keys",
		        newSshServer);

		newSshServer.setHost(this.host);
		this.log.debug("Set host on new SshServer instance {} to {}",
		        newSshServer, this.host);

		newSshServer.setPort(this.port);
		this.log.debug("Set port on new SshServer instance {} to {}",
		        newSshServer, this.port);

		newSshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
		        this.hostKeyPath));
		this.log.debug(
		        "New SshServer instance {} will store its host key at {}",
		        newSshServer, this.hostKeyPath);

		newSshServer.setScheduledExecutorService(this.scheduledExecutorService,
		        true);
		this.log.debug(
		        "Set scheduled executor service on new SshServer instance {} to {}",
		        newSshServer, this.scheduledExecutorService);

		if (this.autostart) {
			this.log.info(
			        "Autostart is set to true - will start newly created SshServer {} ...",
			        newSshServer);
			final long before = System.currentTimeMillis();
			newSshServer.start();
			this.log.info("Started newly created SshServer {} in {} ms",
			        newSshServer, System.currentTimeMillis() - before);
		} else {
			this.log.info(
			        "Autostart is set to false - will NOT start newly created SshServer {}",
			        newSshServer);
		}

		this.product = newSshServer;
		this.log.info("Finished creating new SshServer instance {}",
		        newSshServer);
	}

	private void checkConfig() throws IllegalStateException {
		if (this.shellFactory == null) {
			throw new IllegalStateException("No ShellFactory has been set");
		}
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public final void setHost(final String host) {
		notEmpty(host, "Argument 'host' must neither be null nor empty");
		this.host = host;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public final void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @param hostKeyPath
	 *            the hostKeyPath to set
	 */
	public final void setHostKeyPath(final String hostKeyPath) {
		notEmpty(hostKeyPath,
		        "Argument 'hostKeyPath' must neither be null nor empty");
		this.hostKeyPath = hostKeyPath;
	}

	/**
	 * @param autostart
	 *            the autostart to set
	 */
	public final void setAutostart(final boolean autostart) {
		this.autostart = autostart;
	}

	@Override
	public void setClamshellLauncherFactory(
	        final ClamshellLauncher.Factory clamshellLauncherFactory) {
		notNull(clamshellLauncherFactory,
		        "Argument 'clamshellLauncherFactory' must not be null");
		this.shellFactory = new ClamshellLauncherCommand.Factory(
		        clamshellLauncherFactory);
	}

	/**
	 * @param scheduledExecutorService
	 *            the scheduledExecutorService to set
	 */
	public final void setScheduledExecutorService(
	        final ScheduledExecutorService scheduledExecutorService) {
		notNull(scheduledExecutorService,
		        "Argument 'scheduledExecutorService' must not be null");
		this.scheduledExecutorService = scheduledExecutorService;
	}

	@Override
	public String toString() {
		return "SshServerFactoryBean@" + this.hashCode() + "[host: "
		        + this.host + "|port: " + this.port + "|hostKeyPath: "
		        + this.hostKeyPath + "|autostart: " + this.autostart
		        + "|shellFactory: " + this.shellFactory
		        + "|scheduledExecutorService: " + this.scheduledExecutorService
		        + "|product: " + this.product + "]";
	}
}
