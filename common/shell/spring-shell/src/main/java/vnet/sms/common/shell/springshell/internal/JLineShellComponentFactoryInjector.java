/**
 * 
 */
package vnet.sms.common.shell.springshell.internal;

import static org.apache.commons.lang.Validate.notNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import vnet.sms.common.shell.springshell.JLineShellComponentFactory;
import vnet.sms.common.shell.springshell.JLineShellComponentFactoryAware;

/**
 * @author obergner
 * 
 */
public class JLineShellComponentFactoryInjector implements BeanPostProcessor {

	private final JLineShellComponentFactory	factoryToInject;

	/**
	 * @param factoryToInject
	 */
	public JLineShellComponentFactoryInjector(
	        final JLineShellComponentFactory factoryToInject) {
		notNull(factoryToInject, "Argument 'factoryToInject' must not be null");
		this.factoryToInject = factoryToInject;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		if (JLineShellComponentFactoryAware.class.isInstance(bean)) {
			JLineShellComponentFactoryAware.class.cast(bean)
			        .setJLineShellComponentFactory(this.factoryToInject);
		}
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
	        final String beanName) throws BeansException {
		return bean;
	}

	@Override
	public String toString() {
		return "JLineShellComponentFactoryInjector@" + this.hashCode()
		        + "[factoryToInject: " + this.factoryToInject + "]";
	}
}
