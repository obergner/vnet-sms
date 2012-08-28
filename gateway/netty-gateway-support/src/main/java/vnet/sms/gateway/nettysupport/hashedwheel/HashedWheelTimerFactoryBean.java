/**
 * 
 */
package vnet.sms.gateway.nettysupport.hashedwheel;

import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>
 * {@code FactoryBean} for creating a singleton {@link HashedWheelTimer}
 * instance to be used by all components needing one. This facilitates a best
 * practice that stipulates that only one such instance be created to avoid
 * resource (Thread) drain.
 * <p>
 * 
 * @author obergner <a href="olaf.bergner@gmx.de">Olaf Bergner</a>
 * 
 */
public final class HashedWheelTimerFactoryBean implements
        FactoryBean<HashedWheelTimer>, DisposableBean, InitializingBean {

	private final Logger	       log	    = LoggerFactory
	                                                .getLogger(getClass());

	private final HashedWheelTimer	product	= new HashedWheelTimer();

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.product.start();
		this.log.info("Started {}", this.product);
	}

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		this.product.stop();
		this.log.info(
		        "Stopped {} - all currently running timer tasks (if any) have been cancelled",
		        this.product);
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public HashedWheelTimer getObject() throws Exception {
		return this.product;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product.getClass();
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
}
