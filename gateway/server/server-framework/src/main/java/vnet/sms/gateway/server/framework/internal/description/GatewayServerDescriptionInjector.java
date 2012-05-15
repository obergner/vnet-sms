/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.description;

import static org.apache.commons.lang.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import vnet.sms.gateway.server.framework.GatewayServerDescriptionAware;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;

/**
 * @author obergner
 * 
 */
public class GatewayServerDescriptionInjector implements BeanPostProcessor,
        ApplicationContextAware {

	private final Logger	   log	= LoggerFactory.getLogger(getClass());

	private ApplicationContext	applicationContext;

	/**
	 * @see vnet.sms.common.context.ApplicationContextAware#setApplicationContext(vnet.sms.common.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(
	        final ApplicationContext applicationContext) throws BeansException {
		notNull(applicationContext,
		        "Argument 'applicationContext' must not be null");
		this.applicationContext = applicationContext;
	}

	/**
	 * @see vnet.sms.common.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.trace("Testing if {} implements {} ...", bean,
		        GatewayServerDescriptionAware.class.getName());
		if (bean instanceof GatewayServerDescriptionAware) {
			this.log.debug(
			        "{} DOES implement {} - will inject GatewayServerDescription",
			        bean, GatewayServerDescriptionAware.class.getName());
			final GatewayServerDescription gatewayServerDescription = lookupGatewayServerDescription();
			GatewayServerDescriptionAware.class.cast(bean)
			        .setGatewayServerDescription(gatewayServerDescription);
			this.log.info("Injected {} into {}", gatewayServerDescription, bean);
		} else {
			this.log.trace("{} does NOT implement {} - SKIPPING", bean,
			        GatewayServerDescriptionAware.class.getName());
		}
		return bean;
	}

	private GatewayServerDescription lookupGatewayServerDescription() {
		if (this.applicationContext == null) {
			throw new IllegalStateException(
			        "No ApplicationContext has been set - cannot lookup GatewayServerDescription");
		}
		return this.applicationContext.getBean(GatewayServerDescription.class);
	}

	/**
	 * @see vnet.sms.common.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
	        final String beanName) throws BeansException {
		return bean;
	}
}
