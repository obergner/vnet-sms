/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import org.clamshellcli.api.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author obergner
 * 
 */
public class StaticContextFactory {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	private Configurator	configuration;

	private PluginRegistry	pluginRegistry;

	private CommandRegistry	commandRegistry;

	public StaticContext newContext() {
		final StaticContext newContext = new StaticContext(this.configuration,
		        this.pluginRegistry, this.commandRegistry);
		this.log.info("Created new StaticContext {}", newContext);
		return newContext;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	@Required
	public final void setConfiguration(final Configurator configuration) {
		notNull(configuration, "Argument 'configuration' must not be null");
		this.configuration = configuration;
	}

	/**
	 * @param pluginRegistry
	 *            the pluginRegistry to set
	 */
	@Required
	public final void setPluginRegistry(final PluginRegistry pluginRegistry) {
		notNull(pluginRegistry, "Argument 'pluginRegistry' must not be null");
		this.pluginRegistry = pluginRegistry;
	}

	/**
	 * @param commandRegistry
	 *            the commandRegistry to set
	 */
	@Required
	public final void setCommandRegistry(final CommandRegistry commandRegistry) {
		notNull(commandRegistry, "Argument 'commandRegistry' must not be null");
		this.commandRegistry = commandRegistry;
	}
}
