/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;
import vnet.sms.common.shell.clamshellspring.ClamshellLauncher.Factory;

/**
 * @author obergner
 * 
 */
public class EmbeddedClamshellLauncherFactory implements Factory {

	private final Logger	     log	= LoggerFactory.getLogger(getClass());

	private StaticContextFactory	contextFactory;

	/**
	 * @see vnet.sms.common.shell.clamshellspring.ClamshellLauncher.Factory#newLauncher()
	 */
	@Override
	public ClamshellLauncher newLauncher() {
		final EmbeddedClamshellLauncher newLauncher = new EmbeddedClamshellLauncher(
		        this.contextFactory.newContext());
		this.log.info("Created new ClamshellLauncher {}", newLauncher);
		return newLauncher;
	}

	/**
	 * @param contextFactory
	 *            the contextFactory to set
	 */
	@Required
	public final void setContextFactory(
	        final StaticContextFactory contextFactory) {
		notNull(contextFactory, "Argument 'contextFactory' must not be null");
		this.contextFactory = contextFactory;
	}
}
