/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.Shell;

/**
 * @author obergner
 * 
 */
final class StaticContext implements Context {

	private final Map<String, Object>	values	= new HashMap<String, Object>();

	private final Configurator	      config;

	private final PluginRegistry	  pluginRegistry;

	private final CommandRegistry	  commandRegistry;

	/**
	 * @param config
	 * @param pluginRegistry
	 * @param shell
	 * @param console
	 * @param prompt
	 * @param commandRegistry
	 */
	StaticContext(final Configurator config,
	        final PluginRegistry pluginRegistry,
	        final CommandRegistry commandRegistry) {
		notNull(config, "Argument 'config' must not be null");
		notNull(pluginRegistry, "Argument 'pluginRegistry' must not be null");
		notNull(commandRegistry, "Argument 'commandRegistry' must not be null");
		this.config = config;
		this.pluginRegistry = pluginRegistry;
		this.commandRegistry = commandRegistry;
	}

	/**
	 * @see org.clamshellcli.api.Context#getCommands()
	 */
	@Override
	public List<Command> getCommands() {
		return this.commandRegistry.getCommands();
	}

	/**
	 * @see org.clamshellcli.api.Context#getCommandsByNamespace(java.lang.String)
	 */
	@Override
	public List<Command> getCommandsByNamespace(final String namespace) {
		return this.commandRegistry.getCommandsByNamespace(namespace);
	}

	/**
	 * @see org.clamshellcli.api.Context#mapCommands(java.util.List)
	 */
	@Override
	public Map<String, Command> mapCommands(final List<Command> commands) {
		return this.commandRegistry.mapCommands(commands);
	}

	/**
	 * @see org.clamshellcli.api.Context#getConfigurator()
	 */
	@Override
	public Configurator getConfigurator() {
		return this.config;
	}

	/**
	 * @see org.clamshellcli.api.Context#getIoConsole()
	 */
	@Override
	public IOConsole getIoConsole() {
		return this.pluginRegistry.getIOConsole();
	}

	/**
	 * @see org.clamshellcli.api.Context#getPlugins()
	 */
	@Override
	public List<Plugin> getPlugins() {
		return this.pluginRegistry.getPlugins();
	}

	/**
	 * @see org.clamshellcli.api.Context#getPluginsByType(java.lang.Class)
	 */
	@Override
	public <T> List<T> getPluginsByType(final Class<T> type) {
		final List<T> result = new ArrayList<T>();
		for (final Plugin p : getPlugins()) {
			if (type.isAssignableFrom(p.getClass())) {
				result.add((T) p);
			}
		}
		return this.pluginRegistry.getPluginsByType(type);
	}

	/**
	 * @see org.clamshellcli.api.Context#getPrompt()
	 */
	@Override
	public Prompt getPrompt() {
		return this.pluginRegistry.getPrompt();
	}

	/**
	 * @see org.clamshellcli.api.Context#getShell()
	 */
	@Override
	public Shell getShell() {
		return this.pluginRegistry.getShell();
	}

	/**
	 * @see org.clamshellcli.api.Context#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(final String key) {
		return this.values.get(key);
	}

	/**
	 * @see org.clamshellcli.api.Context#getValues()
	 */
	@Override
	public Map<String, ? extends Object> getValues() {
		return Collections.unmodifiableMap(this.values);
	}

	/**
	 * @see org.clamshellcli.api.Context#putValue(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void putValue(final String key, final Object value) {
		this.values.put(key, value);
	}

	/**
	 * @see org.clamshellcli.api.Context#putValues(java.util.Map)
	 */
	@Override
	public void putValues(final Map<String, ? extends Object> additionalValues) {
		this.values.putAll(additionalValues);
	}

	/**
	 * @see org.clamshellcli.api.Context#removeValue(java.lang.String)
	 */
	@Override
	public void removeValue(final String key) {
		this.values.remove(key);
	}
}
