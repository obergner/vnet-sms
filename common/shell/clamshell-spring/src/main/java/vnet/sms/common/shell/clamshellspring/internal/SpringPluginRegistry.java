/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.Shell;
import org.clamshellcli.api.SplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author obergner
 * 
 */
public class SpringPluginRegistry implements PluginRegistry, BeanPostProcessor {

	private final Logger	   log	    = LoggerFactory.getLogger(getClass());

	private final List<Plugin>	plugins	= new ArrayList<Plugin>();

	@Override
	public List<Plugin> getPlugins() {
		return Collections.unmodifiableList(this.plugins);
	}

	@Override
	public <T> List<T> getPluginsByType(final Class<T> type) {
		final List<T> result = new ArrayList<T>();
		for (final Plugin p : getPlugins()) {
			if (type.isAssignableFrom(p.getClass())) {
				result.add((T) p);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public Shell getShell() {
		final List<Shell> shells = getPluginsByType(Shell.class);
		if (shells.isEmpty()) {
			throw new IllegalStateException("No 'Shell' has been registered");
		}
		if (shells.size() > 1) {
			throw new IllegalStateException(
			        "More than one 'Shell' has been registered");
		}
		return shells.get(0);
	}

	@Override
	public IOConsole getIOConsole() {
		final List<IOConsole> consoles = getPluginsByType(IOConsole.class);
		if (consoles.isEmpty()) {
			throw new IllegalStateException(
			        "No 'IOConsole' has been registered");
		}
		if (consoles.size() > 1) {
			throw new IllegalStateException(
			        "More than one 'IOConsole' has been registered");
		}
		return consoles.get(0);
	}

	@Override
	public Prompt getPrompt() {
		final List<Prompt> prompts = getPluginsByType(Prompt.class);
		if (prompts.isEmpty()) {
			final Prompt usernamePrompt = new UsernamePrompt();
			this.plugins.add(usernamePrompt);
			return getPrompt();
		}
		if (prompts.size() > 1) {
			throw new IllegalStateException(
			        "More than one 'Prompt' has been registered");
		}
		return prompts.get(0);
	}

	private static class UsernamePrompt implements Prompt {

		private final String	value	= System.getProperty("user.name")
		                                      + " > ";

		@Override
		public String getValue(final Context ctx) {
			return this.value;
		}

		@Override
		public void plug(final Context plug) {
		}
	}

	@Override
	public SplashScreen getSplashScreen() {
		final List<SplashScreen> splashScreens = getPluginsByType(SplashScreen.class);
		final SplashScreen result;
		if (splashScreens.isEmpty()) {
			result = null;
		} else if (splashScreens.size() == 1) {
			result = splashScreens.get(0);
		} else {
			throw new IllegalStateException(
			        "More than one 'SplashScreen' has been registered");
		}
		return result;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#
	 *      postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#
	 *      postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.debug(
		        "Testing if bean [name = {}|bean = {}] implements {} and is not a {} ...",
		        new Object[] { bean, beanName, Plugin.class.getName(),
		                Command.class.getName() });
		if (Plugin.class.isInstance(bean) && !Command.class.isInstance(bean)) {
			this.plugins.add(Plugin.class.cast(bean));
			this.log.info(
			        "Bean [name = {}|bean = {}] implements {} and is not a {} - it has been added to the list of known plugins",
			        new Object[] { bean, beanName, Plugin.class.getName(),
			                Command.class.getName() });
		} else {
			this.log.debug(
			        "Bean [name = {}|bean = {}] does NOT implement {} OR is a {} - it will be ignored",
			        new Object[] { bean, beanName, Plugin.class.getName(),
			                Command.class.getName() });
		}
		return bean;
	}
}
