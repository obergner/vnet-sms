/**
 * 
 */
package vnet.sms.common.shell.springshell.plugin;

/**
 * @author obergner
 * 
 */
public interface PluginProvidersRegistry {

	<T extends PluginProvider> T highestPriorityProviderOfType(Class<T> t);
}
