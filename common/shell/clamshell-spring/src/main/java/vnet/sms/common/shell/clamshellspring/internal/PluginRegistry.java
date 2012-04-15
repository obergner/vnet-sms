/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.util.List;

import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.Shell;
import org.clamshellcli.api.SplashScreen;

/**
 * @author obergner
 * 
 */
interface PluginRegistry {

	List<Plugin> getPlugins();

	<T> List<T> getPluginsByType(Class<T> type);

	Shell getShell();

	IOConsole getIOConsole();

	Prompt getPrompt();

	SplashScreen getSplashScreen();
}
