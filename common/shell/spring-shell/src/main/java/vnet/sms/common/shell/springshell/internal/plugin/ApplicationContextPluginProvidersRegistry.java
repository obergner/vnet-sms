/**
 * 
 */
package vnet.sms.common.shell.springshell.internal.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import vnet.sms.common.shell.springshell.plugin.PluginProvider;
import vnet.sms.common.shell.springshell.plugin.PluginProvidersRegistry;

/**
 * @author obergner
 * 
 */
public class ApplicationContextPluginProvidersRegistry implements
        PluginProvidersRegistry, ApplicationContextAware {

	private static final AnnotationAwareOrderComparator	ANNOTATION_ORDER_COMPARATOR	= new AnnotationAwareOrderComparator();

	private ApplicationContext	                        applicationContext;

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(
	        final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * @see vnet.sms.common.shell.springshell.plugin.PluginProvidersRegistry#highestPriorityProviderOfType(java.lang.Class)
	 */
	@Override
	public <T extends PluginProvider> T highestPriorityProviderOfType(
	        final Class<T> t) {
		final Map<String, T> providers = BeanFactoryUtils
		        .beansOfTypeIncludingAncestors(this.applicationContext, t);
		final List<T> sortedProviders = new ArrayList<T>(providers.values());
		Collections.sort(sortedProviders, ANNOTATION_ORDER_COMPARATOR);
		return sortedProviders.get(0);
	}
}
