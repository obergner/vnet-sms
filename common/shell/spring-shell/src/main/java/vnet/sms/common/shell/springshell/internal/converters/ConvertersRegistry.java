/**
 * 
 */
package vnet.sms.common.shell.springshell.internal.converters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import vnet.sms.common.shell.springshell.Converter;

/**
 * @author obergner
 * 
 */
public class ConvertersRegistry implements Iterable<Converter<?>>,
        BeanPostProcessor {

	private final Logger	        log	          = LoggerFactory
	                                                      .getLogger(getClass());

	private final Set<Converter<?>>	allConverters	= new HashSet<Converter<?>>();

	// ------------------------------------------------------------------------
	// API
	// ------------------------------------------------------------------------

	@Override
	public Iterator<Converter<?>> iterator() {
		return getAllConverters().iterator();
	}

	public final Set<Converter<?>> getAllConverters() {
		return Collections.unmodifiableSet(this.allConverters);
	}

	// ------------------------------------------------------------------------
	// BeanPostProcessor
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.trace(
		        "Testing if bean [name = {} | bean = {}] implements [{}] ...",
		        new Object[] { beanName, bean, Converter.class.getName() });
		if (bean instanceof Converter) {
			this.log.info(
			        "Bean [name = {} | bean = {}] implements [{}] - it will be added to the set of known converters",
			        new Object[] { beanName, bean, Converter.class.getName() });
			this.allConverters.add(Converter.class.cast(bean));
		} else {
			this.log.trace(
			        "Bean [name = {} | bean = {}] does NOT implement [{}] - SKIPPING",
			        new Object[] { beanName, bean, Converter.class.getName() });
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
}
