/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author obergner
 * 
 */
public final class SpringCommandRegistry implements CommandRegistry,
        BeanPostProcessor {

	private final Logger	    log	     = LoggerFactory.getLogger(getClass());

	private final List<Command>	commands	= new ArrayList<Command>();

	/**
	 * @see vnet.sms.common.shell.clamshellspring.internal.CommandRegistry#getCommands()
	 */
	@Override
	public List<Command> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}

	/**
	 * @see vnet.sms.common.shell.clamshellspring.internal.CommandRegistry#getCommandsByNamespace(java.lang.String)
	 */
	@Override
	public List<Command> getCommandsByNamespace(final String namespace) {
		final List<Command> result = new ArrayList<Command>();
		for (final Command cmd : this.commands) {
			final Command.Descriptor desc = cmd.getDescriptor();
			if ((desc != null) && desc.getNamespace().equals(namespace)) {
				result.add(cmd);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * @see vnet.sms.common.shell.clamshellspring.internal.CommandRegistry#mapCommands(java.util.List)
	 */
	@Override
	public Map<String, Command> mapCommands(final List<Command> commands) {
		final Map<String, Command> cmdMap = new HashMap<String, Command>();
		for (final Command cmd : commands) {
			final Command.Descriptor desc = cmd.getDescriptor();
			if ((desc != null) && (desc.getName() != null)) {
				cmdMap.put(desc.getName(), cmd);
			}
		}
		return Collections.unmodifiableMap(cmdMap);
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.debug(
		        "Testing if bean [name = {}|bean = {}] implements {} ...",
		        new Object[] { bean, beanName, Command.class.getName() });
		if (Command.class.isInstance(bean)) {
			this.commands.add(Command.class.cast(bean));
			this.log.info(
			        "Bean [name = {}|bean = {}] implements {} - it has been added to the list of known commands",
			        new Object[] { bean, beanName, Command.class.getName() });
		} else {
			this.log.debug(
			        "Bean [name = {}|bean = {}] does NOT implement {} - it will be ignored",
			        new Object[] { bean, beanName, Command.class.getName() });
		}
		return bean;
	}
}
