/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.channel;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.MBeanExportOperations;

import vnet.sms.common.spring.jmx.MBeanExportOperationsAware;
import vnet.sms.gateway.nettysupport.monitor.MonitoringChannelGroup;
import vnet.sms.gateway.server.framework.GatewayServerDescriptionAware;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;

import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class ChannelGroupFactory implements FactoryBean<ChannelGroup>,
        InitializingBean, DisposableBean, MBeanExportOperationsAware,
        GatewayServerDescriptionAware {

	private final Logger	         log	= LoggerFactory
	                                             .getLogger(getClass());

	private MBeanExportOperations	 mbeanExporter;

	private MetricsRegistry	         metricsRegistry;

	private GatewayServerDescription	description;

	private ChannelGroup	         product;

	// ------------------------------------------------------------------------
	// GatewayServerDescriptionAware
	// ------------------------------------------------------------------------

	@Override
	public void setGatewayServerDescription(
	        final GatewayServerDescription gatewayServerDescription) {
		notNull(gatewayServerDescription,
		        "Argument 'gatewayServerDescription' must not be null");
		this.description = gatewayServerDescription;
	}

	// ------------------------------------------------------------------------
	// MBeanExportOperationsAware
	// ------------------------------------------------------------------------

	@Override
	public void setMBeanExportOperations(
	        final MBeanExportOperations mbeanExportOperations) {
		notNull(mbeanExportOperations,
		        "Argument 'mbeanExportOperations' must not be null");
		this.mbeanExporter = mbeanExportOperations;
	}

	// ------------------------------------------------------------------------
	// Set MetricsRegistry
	// ------------------------------------------------------------------------

	@Required
	public void setMetricsRegistry(final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	// ------------------------------------------------------------------------
	// InitializingBean
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.common.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.description == null) {
			throw new IllegalStateException("No description has been set");
		}
		if (this.mbeanExporter == null) {
			throw new IllegalStateException(
			        "No MBeanExportOperations has been set");
		}
		if (this.metricsRegistry == null) {
			throw new IllegalStateException("No MetricsRegistry has been set");
		}
		final String name = this.description.toString()
		        + " - All Connected Channels";
		this.product = new MonitoringChannelGroup(name, this.mbeanExporter,
		        this.metricsRegistry);
		this.log.info("Created new ChannelGroup {}", this.product);
	}

	// ------------------------------------------------------------------------
	// FactoryBean
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.common.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public ChannelGroup getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No ChannelGroup has been created yet - did you remember to call afterPropertiesSet() when using this class outside Spring?");
		}
		return this.product;
	}

	/**
	 * @see vnet.sms.common.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass()
		        : ChannelGroup.class;
	}

	/**
	 * @see vnet.sms.common.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	// ------------------------------------------------------------------------
	// DisposableBean
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.common.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		this.log.info(
		        "Attempting to close all channels in ChannelGroup {} - expecting all channels to be already closed",
		        this.product);
		this.product.close().addListener(new ChannelGroupFutureListener() {

			@Override
			public void operationComplete(final ChannelGroupFuture future)
			        throws Exception {
				if (!future.isCompleteSuccess()) {
					ChannelGroupFactory.this.log
					        .warn("Not all channels in {} could be successfully closed - IGNORING",
					                future.getGroup());
				} else {
					ChannelGroupFactory.this.log.info(
					        "All channels in {} have been successfully closed",
					        future.getGroup());
				}
			}
		});
	}
}
