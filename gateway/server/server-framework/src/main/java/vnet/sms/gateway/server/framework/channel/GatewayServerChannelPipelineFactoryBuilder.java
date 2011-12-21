/**
 * 
 */
package vnet.sms.gateway.server.framework.channel;

import java.io.Serializable;

import javax.management.MBeanServer;

import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

/**
 * @author obergner
 * 
 */
public class GatewayServerChannelPipelineFactoryBuilder<ID extends Serializable, TP>
        implements FactoryBean<GatewayServerChannelPipelineFactory<ID, TP>>,
        InitializingBean {

	private final Logger	                                          log	= LoggerFactory
	                                                                              .getLogger(getClass());

	private Class<TP>	                                              pduType;

	private FrameDecoder	                                          frameDecoder;

	private OneToOneDecoder	                                          decoder;

	private OneToOneEncoder	                                          encoder;

	private TransportProtocolAdaptingUpstreamChannelHandler<ID, TP>	  upstreamTransportProtocolAdapter;

	private TransportProtocolAdaptingDownstreamChannelHandler<ID, TP>	downstreamTransportProtocolAdapter;

	private ChannelMonitorRegistry	                                  channelMonitorRegistry;

	private int	                                                      availableIncomingWindows;

	private long	                                                  incomingWindowWaitTimeMillis;

	private AuthenticationManager	                                  authenticationManager;

	private long	                                                  failedLoginResponseDelayMillis;

	private MessageReferenceGenerator<ID>	                          windowIdGenerator;

	private int	                                                      pingIntervalSeconds;

	private long	                                                  pingResponseTimeoutMillis;

	private MBeanServer	                                              mbeanServer;

	private GatewayServerChannelPipelineFactory<ID, TP>	              producedPipelineFactory;

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
		if (this.producedPipelineFactory != null) {
			this.log.warn(
			        "Already built GatewayServerChannelPipelineFactory instance {}",
			        this.producedPipelineFactory);
			return;
		}

		this.producedPipelineFactory = new GatewayServerChannelPipelineFactory<ID, TP>(
		        this.pduType, this.frameDecoder, this.decoder, this.encoder,
		        this.upstreamTransportProtocolAdapter,
		        this.downstreamTransportProtocolAdapter,
		        this.channelMonitorRegistry, this.availableIncomingWindows,
		        this.incomingWindowWaitTimeMillis, this.authenticationManager,
		        this.failedLoginResponseDelayMillis, this.windowIdGenerator,
		        this.pingIntervalSeconds, this.pingResponseTimeoutMillis,
		        this.mbeanServer);

		this.log.info(
		        "Finished building GatewayServerChannelPipelineFactory instance {}",
		        this.producedPipelineFactory);
	}

	/**
	 * @param pduType
	 *            the pduType to set
	 */
	public final void setPduType(final Class<TP> pduType) {
		this.pduType = pduType;
	}

	/**
	 * @param frameDecoder
	 *            the frameDecoder to set
	 */
	public final void setFrameDecoder(final FrameDecoder frameDecoder) {
		this.frameDecoder = frameDecoder;
	}

	/**
	 * @param decoder
	 *            the decoder to set
	 */
	public final void setDecoder(final OneToOneDecoder decoder) {
		this.decoder = decoder;
	}

	/**
	 * @param encoder
	 *            the encoder to set
	 */
	public final void setEncoder(final OneToOneEncoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @param upstreamTransportProtocolAdapter
	 *            the upstreamTransportProtocolAdapter to set
	 */
	public final void setUpstreamTransportProtocolAdapter(
	        final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP> upstreamTransportProtocolAdapter) {
		this.upstreamTransportProtocolAdapter = upstreamTransportProtocolAdapter;
	}

	/**
	 * @param downstreamTransportProtocolAdapter
	 *            the downstreamTransportProtocolAdapter to set
	 */
	public final void setDownstreamTransportProtocolAdapter(
	        final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP> downstreamTransportProtocolAdapter) {
		this.downstreamTransportProtocolAdapter = downstreamTransportProtocolAdapter;
	}

	/**
	 * @param channelMonitorRegistry
	 *            the channelMonitorRegistry to set
	 */
	public final void setChannelMonitorRegistry(
	        final ChannelMonitorRegistry channelMonitorRegistry) {
		this.channelMonitorRegistry = channelMonitorRegistry;
	}

	/**
	 * @param availableIncomingWindows
	 *            the availableIncomingWindows to set
	 */
	public final void setAvailableIncomingWindows(
	        final int availableIncomingWindows) {
		this.availableIncomingWindows = availableIncomingWindows;
	}

	/**
	 * @param incomingWindowWaitTimeMillis
	 *            the incomingWindowWaitTimeMillis to set
	 */
	public final void setIncomingWindowWaitTimeMillis(
	        final long incomingWindowWaitTimeMillis) {
		this.incomingWindowWaitTimeMillis = incomingWindowWaitTimeMillis;
	}

	/**
	 * @param authenticationManager
	 *            the authenticationManager to set
	 */
	public final void setAuthenticationManager(
	        final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	/**
	 * @param failedLoginResponseDelayMillis
	 *            the failedLoginResponseDelayMillis to set
	 */
	public final void setFailedLoginResponseDelayMillis(
	        final long failedLoginResponseDelayMillis) {
		this.failedLoginResponseDelayMillis = failedLoginResponseDelayMillis;
	}

	/**
	 * @param windowIdGenerator
	 *            the windowIdGenerator to set
	 */
	public final void setWindowIdGenerator(
	        final MessageReferenceGenerator<ID> windowIdGenerator) {
		this.windowIdGenerator = windowIdGenerator;
	}

	/**
	 * @param pingIntervalSeconds
	 *            the pingIntervalSeconds to set
	 */
	public final void setPingIntervalSeconds(final int pingIntervalSeconds) {
		this.pingIntervalSeconds = pingIntervalSeconds;
	}

	/**
	 * @param pingResponseTimeoutMillis
	 *            the pingResponseTimeoutMillis to set
	 */
	public final void setPingResponseTimeoutMillis(
	        final long pingResponseTimeoutMillis) {
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
	}

	/**
	 * @param mbeanServer
	 *            the mbeanServer to set
	 */
	public final void setMbeanServer(final MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}
}
