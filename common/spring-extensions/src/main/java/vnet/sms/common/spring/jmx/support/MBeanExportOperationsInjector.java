/**
 * 
 */
package vnet.sms.common.spring.jmx.support;

import static org.apache.commons.lang.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.MBeanExportOperations;

import vnet.sms.common.spring.jmx.MBeanExportOperationsAware;

/**
 * @author obergner
 * 
 */
public class MBeanExportOperationsInjector implements BeanPostProcessor,
        ApplicationContextAware {

	private final Logger	   log	= LoggerFactory.getLogger(getClass());

	private ApplicationContext	applicationContext;

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(
	        final ApplicationContext applicationContext) throws BeansException {
		notNull(applicationContext,
		        "Argument 'applicationContext' must not be null");
		this.applicationContext = applicationContext;
	}

	private ApplicationContext getMandatoryApplicationContext()
	        throws IllegalStateException {
		if (this.applicationContext == null) {
			throw new IllegalStateException(
			        "No ApplicationContext has been set. Are you using this class outside Spring and forgot to explicitly set an ApplicationContext?");
		}
		return this.applicationContext;
	}

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
		                MBeanExportOperationsAware.class.getName() });
		if (bean instanceof MBeanExportOperationsAware) {
			this.log.info(
			        "Bean [name = {} | bean = {}] implements [{}] - will inject [{}] instance",
			        new Object[] { beanName, bean,
			                MBeanExportOperationsAware.class.getName(),
			                MBeanExportOperations.class.getName() });
			MBeanExportOperationsAware.class.cast(bean)
			        .setMBeanExportOperations(
			                getMandatoryMBeanExportOperations());
		} else {
			this.log.trace(
			        "Bean [name = {} | bean = {}] does NOT implement [{}] - skipping",
			        new Object[] { beanName, bean,
			                MBeanExportOperationsAware.class.getName() });
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

	private MBeanExportOperations getMandatoryMBeanExportOperations()
	        throws IllegalStateException {
		if (getMandatoryApplicationContext().getBeansOfType(
		        MBeanExportOperations.class).isEmpty()) {
			throw new IllegalStateException("No implementation of ["
			        + MBeanExportOperations.class.getName()
			        + "] has been registered in ApplicationContext ["
			        + getMandatoryApplicationContext()
			        + "]. Please register an MBeanExporter.");
		}
		return getMandatoryApplicationContext().getBean(
		        MBeanExportOperations.class);
	}
}
