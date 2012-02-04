/**
 * 
 */
package vnet.sms.gateway.server.framework.channel;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.spring.jmx.MBeanExportOperationsAware;
import vnet.sms.gateway.nettysupport.monitor.incoming.InitialChannelEventsMonitor;
import vnet.sms.gateway.server.framework.jmsbridge.IncomingMessagesForwardingJmsBridge;
import vnet.sms.gateway.transport.plugin.TransportProtocolExtensionPoint;
import vnet.sms.gateway.transport.plugin.context.TransportProtocolPluginInjector;
import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;

/**
 * @author obergner
 * 
 */
public class GatewayServerChannelPipelineFactoryBuilder<ID extends Serializable, TP>
        implements FactoryBean<GatewayServerChannelPipelineFactory<ID, TP>>,
        InitializingBean, TransportProtocolExtensionPoint<ID, TP>,
        MBeanExportOperationsAware {

	private final Logger	                            log	= LoggerFactory
	                                                                .getLogger(getClass());

	private String	                                    gatewayServerInstanceId;

	private TransportProtocolPlugin<ID, TP>	            transportProtocolPlugin;

	private int	                                        availableIncomingWindows;

	private long	                                    incomingWindowWaitTimeMillis;

	private AuthenticationManager	                    authenticationManager;

	private long	                                    failedLoginResponseDelayMillis;

	private int	                                        pingIntervalSeconds;

	private long	                                    pingResponseTimeoutMillis;

	private MBeanExportOperations	                    mbeanExporter;

	private ChannelGroup	                            allConnectedChannels;

	private IncomingMessagesForwardingJmsBridge<ID>	    messageForwardingJmsBridge;

	private InitialChannelEventsMonitor	                initialChannelEventsMonitor;

	private GatewayServerChannelPipelineFactory<ID, TP>	producedPipelineFactory;

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public GatewayServerChannelPipelineFactory<ID, TP> getObject()
	        throws Exception {
		if (this.producedPipelineFactory == null) {
			throw new IllegalStateException(
			        "No GatewayServerChannelPipelineFactory has been built yet. Did you remember to call 'afterPropertiesSet()' when using this class outside a Spring application context?");
		}
		return this.producedPipelineFactory;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.producedPipelineFactory != null ? this.producedPipelineFactory
		        .getClass() : GatewayServerChannelPipelineFactory.class;
	}

	/**
	 * Returns {@code true} as a {@link GatewayServerChannelPipelineFactory} is
	 * a singleton.
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.log.info("Starting to build GatewayServerChannelPipelineFactory instance ...");
		enforceCorrectUsage();

		this.producedPipelineFactory = new GatewayServerChannelPipelineFactory<ID, TP>(
		        this.gatewayServerInstanceId,
		        this.transportProtocolPlugin.getPduType(),
		        this.transportProtocolPlugin.getFrameDecoder(),
		        this.transportProtocolPlugin.getDecoder(),
		        this.transportProtocolPlugin.getEncoder(),
		        this.transportProtocolPlugin
		                .getPduToWindowedMessageEventConverter(),
		        this.transportProtocolPlugin
		                .getWindowedMessageEventToPduConverter(),
		        this.messageForwardingJmsBridge, this.availableIncomingWindows,
		        this.incomingWindowWaitTimeMillis, this.authenticationManager,
		        this.failedLoginResponseDelayMillis,
		        this.transportProtocolPlugin.getMessageReferenceGenerator(),
		        this.pingIntervalSeconds, this.pingResponseTimeoutMillis,
		        this.mbeanExporter, this.initialChannelEventsMonitor,
		        this.allConnectedChannels);

		this.log.info(
		        "Finished building GatewayServerChannelPipelineFactory instance {}",
		        this.producedPipelineFactory);
	}

	private void enforceCorrectUsage() throws IllegalStateException {
		if (this.producedPipelineFactory != null) {
			throw new IllegalStateException(
			        "Illegal attempt to build GatewayServerChannelPipelineFactory twice");
		}
		if (this.transportProtocolPlugin == null) {
			throw new IllegalStateException(
			        "No implementation of ["
			                + TransportProtocolPlugin.class.getName()
			                + "] has been injected into this factory. Did you remember to register a ["
			                + TransportProtocolPluginInjector.class.getName()
			                + "] as a BeanPostProcessor in this ApplicationContext?");
		}
	}

	@Override
	public void plugin(final TransportProtocolPlugin<ID, TP> plugin) {
		notNull(plugin, "Argument 'plugin' may not be null");
		this.transportProtocolPlugin = plugin;
	}

	@Override
	public void setMBeanExportOperations(
	        final MBeanExportOperations mbeanExportOperations) {
		notNull(mbeanExportOperations,
		        "Argument 'mbeanExportOperations' must not be null");
		this.mbeanExporter = mbeanExportOperations;
	}

	/**
	 * @param gatewayServerInstanceId
	 *            the gatewayServerInstanceId to set
	 */
	@Required
	public final void setGatewayServerInstanceId(
	        final String gatewayServerInstanceId) {
		this.gatewayServerInstanceId = gatewayServerInstanceId;
	}

	/**
	 * @param allConnectedChannels
	 *            the allConnectedChannels to set
	 */
	@Required
	public final void setAllConnectedChannels(
	        final ChannelGroup allConnectedChannels) {
		notNull(allConnectedChannels,
		        "Argument 'allConnectedChannels' must not be null");
		this.allConnectedChannels = allConnectedChannels;
	}

	/**
	 * @param messageForwardingJmsBridge
	 *            the messageForwardingJmsBridge to set
	 */
	@Required
	public final void setMessageForwardingJmsBridge(
	        final IncomingMessagesForwardingJmsBridge<ID> messageForwardingJmsBridge) {
		notNull(messageForwardingJmsBridge,
		        "Argument 'messageForwardingJmsBridge' must not be null");
		this.messageForwardingJmsBridge = messageForwardingJmsBridge;
	}

	/**
	 * @param initialChannelEventsMonitor
	 *            the initialChannelEventsMonitor to set
	 */
	@Required
	public final void setInitialChannelEventsMonitor(
	        final InitialChannelEventsMonitor initialChannelEventsMonitor) {
		notNull(initialChannelEventsMonitor,
		        "Argument 'initialChannelEventsMonitor' must not be null");
		this.initialChannelEventsMonitor = initialChannelEventsMonitor;
	}

	/**
	 * @param availableIncomingWindows
	 *            the availableIncomingWindows to set
	 */
	@Required
	public final void setAvailableIncomingWindows(
	        final int availableIncomingWindows) {
		this.availableIncomingWindows = availableIncomingWindows;
	}

	/**
	 * @param incomingWindowWaitTimeMillis
	 *            the incomingWindowWaitTimeMillis to set
	 */
	@Required
	public final void setIncomingWindowWaitTimeMillis(
	        final long incomingWindowWaitTimeMillis) {
		this.incomingWindowWaitTimeMillis = incomingWindowWaitTimeMillis;
	}

	/**
	 * @param authenticationManager
	 *            the authenticationManager to set
	 */
	@Required
	public final void setAuthenticationManager(
	        final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	/**
	 * @param failedLoginResponseDelayMillis
	 *            the failedLoginResponseDelayMillis to set
	 */
	@Required
	public final void setFailedLoginResponseDelayMillis(
	        final long failedLoginResponseDelayMillis) {
		this.failedLoginResponseDelayMillis = failedLoginResponseDelayMillis;
	}

	/**
	 * @param pingIntervalSeconds
	 *            the pingIntervalSeconds to set
	 */
	@Required
	public final void setPingIntervalSeconds(final int pingIntervalSeconds) {
		this.pingIntervalSeconds = pingIntervalSeconds;
	}

	/**
	 * @param pingResponseTimeoutMillis
	 *            the pingResponseTimeoutMillis to set
	 */
	@Required
	public final void setPingResponseTimeoutMillis(
	        final long pingResponseTimeoutMillis) {
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
	}
}
