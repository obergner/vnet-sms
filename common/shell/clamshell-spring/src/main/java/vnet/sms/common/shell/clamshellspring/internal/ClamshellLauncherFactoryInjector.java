/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanPostProcessor;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;
import vnet.sms.common.shell.clamshellspring.ClamshellLauncherFactoryAware;

/**
 * @author obergner
 * 
 */
public final class ClamshellLauncherFactoryInjector implements
        BeanPostProcessor {

	private final Logger	          log	= LoggerFactory
	                                              .getLogger(getClass());

	private ClamshellLauncher.Factory	clamshellLauncherFactory;

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
		        new Object[] { bean, beanName,
		                ClamshellLauncherFactoryAware.class.getName() });
		if (ClamshellLauncherFactoryAware.class.isInstance(bean)) {
			final ClamshellLauncher.Factory clamshellLauncherFact = getMandatoryClamshellLauncherFactory();
			ClamshellLauncherFactoryAware.class.cast(bean)
			        .setClamshellLauncherFactory(clamshellLauncherFact);
			this.log.info(
			        "Bean [name = {}|bean = {}] implements {} - ClamshellLauncher.Factory [{}] has been injected into it",
			        new Object[] { bean, beanName,
			                ClamshellLauncherFactoryAware.class.getName(),
			                clamshellLauncherFact });
		} else {
			this.log.debug(
			        "Bean [name = {}|bean = {}] does NOT implement {} - it will be ignored",
			        new Object[] { bean, beanName,
			                ClamshellLauncherFactoryAware.class.getName() });
		}
		return bean;
	}

	/**
	 * @param clamshellLauncherFactory
	 *            the clamshellLauncherFactory to set
	 */
	@Required
	public void setClamshellLauncherFactory(
	        final ClamshellLauncher.Factory clamshellLauncherFactory) {
		notNull(clamshellLauncherFactory,
		        "Argument 'clamshellLauncherFactory' must not be null");
		this.clamshellLauncherFactory = clamshellLauncherFactory;
	}

	protected ClamshellLauncher.Factory getMandatoryClamshellLauncherFactory()
	        throws IllegalStateException {
		if (this.clamshellLauncherFactory == null) {
			throw new IllegalStateException(
			        "No ClamshellLauncher.Factory instance has been set");
		}
		return this.clamshellLauncherFactory;
	}
}
