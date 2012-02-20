/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.executor;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.executor.thread.MessageDiagnosticContextPopulatingThreadFactory;
import vnet.sms.gateway.server.framework.GatewayServerDescriptionAware;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;

/**
 * @author obergner
 * 
 */
public class GatewayServerDescriptionMdcThreadFactoryBuilder implements
        FactoryBean<ThreadFactory>, InitializingBean,
        GatewayServerDescriptionAware {

	private static final String	     GATEWAY_SERVER_DESCRIPTION_MDC_KEY	= "gatewayServerDescription";

	private GatewayServerDescription	gatewayServerDescription;

	private String	                 threadNamePrefix;

	private int	                     threadPriority	                    = Thread.NORM_PRIORITY;

	private boolean	                 daemon	                            = false;

	private ThreadGroup	             threadGroup;

	private ThreadFactory	         product;

	// ------------------------------------------------------------------------
	// GatewayServerDescriptionAware
	// ------------------------------------------------------------------------

	@Override
	public void setGatewayServerDescription(
	        final GatewayServerDescription gatewayServerDescription) {
		notNull(gatewayServerDescription,
		        "Argument 'gatewayServerDescription' must not be null");
		this.gatewayServerDescription = gatewayServerDescription;
	}

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/**
	 * @param threadNamePrefix
	 *            the threadNamePrefix to set
	 */
	@Required
	public final void setThreadNamePrefix(final String threadNamePrefix) {
		notEmpty(threadNamePrefix,
		        "Argument 'threadNamePrefix' must be neither null nor empty");
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * @param threadPriority
	 *            the threadPriority to set
	 */
	public final void setThreadPriority(final int threadPriority) {
		this.threadPriority = threadPriority;
	}

	/**
	 * @param daemon
	 *            the daemon to set
	 */
	public final void setDaemon(final boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * @param threadGroup
	 *            the threadGroup to set
	 */
	public final void setThreadGroup(final ThreadGroup threadGroup) {
		notNull(threadGroup, "Argument 'threadGroup' must not be null");
		this.threadGroup = threadGroup;
	}

	public final void setThreadGroupName(final String threadGroupName) {
		notEmpty(threadGroupName,
		        "Argument 'threadGroupName' must not be neither null nor empty. Got: "
		                + threadGroupName);
		setThreadGroup(new ThreadGroup(threadGroupName));
	}

	// ------------------------------------------------------------------------
	// Factory
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.threadNamePrefix == null) {
			throw new IllegalStateException("No threadNamePrefix has been set");
		}
		if (this.gatewayServerDescription == null) {
			throw new IllegalStateException(
			        "No GatewayServerDescription has been set");
		}
		final MessageDiagnosticContextPopulatingThreadFactory threadFactory = new MessageDiagnosticContextPopulatingThreadFactory();
		threadFactory.setThreadNamePrefix(this.threadNamePrefix);
		threadFactory.setDaemon(this.daemon);
		threadFactory.setThreadPriority(this.threadPriority);
		if (this.threadGroup != null) {
			threadFactory.setThreadGroup(this.threadGroup);
		}
		final Map<String, String> mdc = new HashMap<String, String>();
		mdc.put(GATEWAY_SERVER_DESCRIPTION_MDC_KEY,
		        this.gatewayServerDescription.toString());
		threadFactory.setMessageDiagnosticContextParameters(mdc);
		this.product = threadFactory;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public ThreadFactory getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No ThreadFactory has been created yet - did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		}
		return this.product;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass()
		        : ThreadFactory.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
}
