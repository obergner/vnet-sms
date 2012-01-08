/**
 * 
 */
package vnet.sms.gateway.transport.plugin.context;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import vnet.sms.gateway.transport.plugin.TransportProtocolExtensionPoint;
import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;

/**
 * @author obergner
 * 
 */
public class TransportProtocolPluginInjector implements BeanPostProcessor,
        ApplicationContextAware {

	private final Logger	   log	= LoggerFactory.getLogger(getClass());

	private ApplicationContext	applicationContext;

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.trace(
		        "Testing if bean [name = {} | bean = {}] implements [{}] ...",
		        new Object[] { beanName, bean,
		                TransportProtocolExtensionPoint.class.getName() });
		if (TransportProtocolExtensionPoint.class.isInstance(bean)) {
			this.log.trace(
			        "Bean [name = {} | bean = {}] implements [{}] - will inject TransportProtocolPlugin",
			        new Object[] { beanName, bean,
			                TransportProtocolExtensionPoint.class.getName() });

			final TransportProtocolPlugin<? extends Serializable, ?> transportProtocolPlugin = lookupUniqueTransportProtocolPlugin();
			TransportProtocolExtensionPoint.class.cast(bean).plugin(
			        transportProtocolPlugin);

			this.log.info(
			        "Injected TransportProtocolPlugin {} into bean [name = {} | bean = {}]",
			        new Object[] { transportProtocolPlugin, beanName, bean });
		} else {
			this.log.trace(
			        "Bean [name = {} | bean = {}] does NOT implement [{}] - skipping",
			        new Object[] { beanName, bean,
			                TransportProtocolExtensionPoint.class.getName() });
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
	public void setApplicationContext(
	        final ApplicationContext applicationContext) throws BeansException {
		notNull(applicationContext,
		        "Argument 'applicationContext' must not be null");
		this.applicationContext = applicationContext;
	}

	private TransportProtocolPlugin<? extends Serializable, ?> lookupUniqueTransportProtocolPlugin() {
		return getMandatoryApplicationContext().getBean(
		        TransportProtocolPlugin.class);
	}

	private ApplicationContext getMandatoryApplicationContext() {
		if (this.applicationContext == null) {
			throw new IllegalStateException(
			        "No ApplicationContext has been set");
		}
		return this.applicationContext;
	}
}
