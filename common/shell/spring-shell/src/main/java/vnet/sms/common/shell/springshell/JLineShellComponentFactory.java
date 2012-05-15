/**
 * 
 */
package vnet.sms.common.shell.springshell;

import java.io.InputStream;
import java.io.OutputStream;

import vnet.sms.common.shell.springshell.plugin.PluginProvidersRegistry;

/**
 * @author obergner
 * 
 */
public class JLineShellComponentFactory {

	private final Parser	              parser;

	private final PluginProvidersRegistry	pluginProvidersRegistry;

	/**
	 * @param parser
	 */
	public JLineShellComponentFactory(
	        final PluginProvidersRegistry pluginProvidersRegistry,
	        final Parser parser) {
		this.pluginProvidersRegistry = pluginProvidersRegistry;
		this.parser = parser;
	}

	public final JLineShellComponent newShell(final InputStream input,
	        final OutputStream output) {
		return new JLineShellComponent(this.pluginProvidersRegistry, input,
		        output, this.parser);
	}
}
