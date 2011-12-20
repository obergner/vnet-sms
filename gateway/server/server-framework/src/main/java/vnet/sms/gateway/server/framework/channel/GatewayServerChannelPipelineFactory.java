/**
 * 
 */
package vnet.sms.gateway.server.framework.channel;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import javax.management.MBeanServer;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.gateway.nettysupport.login.incoming.IncomingLoginRequestsChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.monitor.incoming.IncomingBytesCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.incoming.IncomingPdusCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.outgoing.OutgoingBytesCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.outgoing.OutgoingPdusCountingChannelHandler;
import vnet.sms.gateway.nettysupport.ping.outgoing.OutgoingPingChannelHandler;
import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.WindowingChannelHandler;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = "vnet.sms.gateway:service=ChannelPipelineFactory,type=Server", description = "Netty ChannelPipelineFactory for attaching a pipeline of channel handlers to each newly connected channel")
public class GatewayServerChannelPipelineFactory<ID extends Serializable, TP>
        implements ChannelPipelineFactory {

	private final Class<TP>	                                                pduType;

	private final FrameDecoder	                                            frameDecoder;

	private final OneToOneDecoder	                                        decoder;

	private final OneToOneEncoder	                                        encoder;

	private final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP>	upstreamTransportProtocolAdapter;

	private final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP>	downstreamTransportProtocolAdapter;

	private final ChannelMonitorRegistry	                                channelMonitorRegistry;

	private final int	                                                    availableIncomingWindows;

	private final long	                                                    incomingWindowWaitTimeMillis;

	private final AuthenticationManager	                                    authenticationManager;

	private final long	                                                    failedLoginResponseDelayMillis;

	private final MessageReferenceGenerator<ID>	                            windowIdGenerator;

	private final int	                                                    pingIntervalSeconds;

	private final long	                                                    pingResponseTimeoutMillis;

	private final MBeanServer	                                            mbeanServer;

	public GatewayServerChannelPipelineFactory(
	        final Class<TP> pduType,
	        final FrameDecoder frameDecoder,
	        final OneToOneDecoder decoder,
	        final OneToOneEncoder encoder,
	        final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP> upstreamTransportProtocolAdapter,
	        final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP> downstreamTransportProtocolAdapter,
	        final ChannelMonitorRegistry channelMonitorRegistry,
	        final int availableIncomingWindows,
	        final long incomingWindowWaitTimeMillis,
	        final AuthenticationManager authenticationManager,
	        final long failedLoginResponseDelayMillis,
	        final MessageReferenceGenerator<ID> windowIdGenerator,
	        final int pingIntervalSeconds,
	        final long pingResponseTimeoutMillis, final MBeanServer mbeanServer) {
		notNull(pduType, "Argument 'pduType' must not be null");
		notNull(frameDecoder, "Argument 'frameDecoder' must not be null");
		notNull(encoder, "Argument 'encoder' must not be null");
		notNull(upstreamTransportProtocolAdapter,
		        "Argument 'upstreamTransportProtocolAdapter' must not be null");
		notNull(downstreamTransportProtocolAdapter,
		        "Argument 'downstreamTransportProtocolAdapter' must not be null");
		notNull(channelMonitorRegistry,
		        "Argument 'channelMonitorRegistry' must not be null");
		notNull(authenticationManager,
		        "Argument 'authenticationManager' must not be null");
		notNull(windowIdGenerator,
		        "Argument 'windowIdGenerator' must not be null");
		notNull(mbeanServer, "Argument 'mbeanServer' must not be null");
		this.pduType = pduType;
		this.frameDecoder = frameDecoder;
		this.decoder = decoder;
		this.encoder = encoder;
		this.upstreamTransportProtocolAdapter = upstreamTransportProtocolAdapter;
		this.downstreamTransportProtocolAdapter = downstreamTransportProtocolAdapter;
		this.channelMonitorRegistry = channelMonitorRegistry;
		this.availableIncomingWindows = availableIncomingWindows;
		this.incomingWindowWaitTimeMillis = incomingWindowWaitTimeMillis;
		this.authenticationManager = authenticationManager;
		this.failedLoginResponseDelayMillis = failedLoginResponseDelayMillis;
		this.windowIdGenerator = windowIdGenerator;
		this.pingIntervalSeconds = pingIntervalSeconds;
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
		this.mbeanServer = mbeanServer;
	}

	/**
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final ChannelPipeline pipeline = Channels.pipeline();

		// Monitor number of incoming bytes ...
		pipeline.addLast(IncomingBytesCountingChannelHandler.NAME,
		        new IncomingBytesCountingChannelHandler(
		                this.channelMonitorRegistry));
		// ... and outgoing bytes, too, while we are at it.
		pipeline.addLast(OutgoingBytesCountingChannelHandler.NAME,
		        new OutgoingBytesCountingChannelHandler(
		                this.channelMonitorRegistry));

		// Frame decoder, decoder, encoder
		pipeline.addLast("vnet.sms.gateway:frame-decoder", this.frameDecoder);
		// May be null in case of object serialization where the decoder IS the
		// frame decoder
		if (this.decoder != null) {
			pipeline.addLast("vnet.sms.gateway:decoder", this.decoder);
		}
		pipeline.addLast("vnet.sms.gateway:encoder", this.encoder);

		// Monitor number of incoming PDUs ...
		pipeline.addLast(IncomingPdusCountingChannelHandler.NAME,
		        new IncomingPdusCountingChannelHandler<TP>(
		                this.channelMonitorRegistry, this.pduType));
		// ... and outgoing PDUs, too, while we are at it.
		pipeline.addLast(OutgoingPdusCountingChannelHandler.NAME,
		        new OutgoingPdusCountingChannelHandler<TP>(
		                this.channelMonitorRegistry, this.pduType));

		// Transport protocol adapter
		pipeline.addLast(TransportProtocolAdaptingUpstreamChannelHandler.NAME,
		        this.upstreamTransportProtocolAdapter);
		pipeline.addLast(
		        TransportProtocolAdaptingDownstreamChannelHandler.NAME,
		        this.downstreamTransportProtocolAdapter);

		// Windowing channel handler
		pipeline.addLast(WindowingChannelHandler.NAME,
		        new WindowingChannelHandler<Serializable>(
		                new IncomingWindowStore<Serializable>(
		                        this.availableIncomingWindows,
		                        this.incomingWindowWaitTimeMillis),
		                this.mbeanServer));

		// Login channel handler
		pipeline.addLast(IncomingLoginRequestsChannelHandler.NAME,
		        new IncomingLoginRequestsChannelHandler<ID>(
		                this.authenticationManager,
		                this.failedLoginResponseDelayMillis));

		// Ping channel handler
		pipeline.addLast(OutgoingPingChannelHandler.NAME,
		        new OutgoingPingChannelHandler<ID>(this.pingIntervalSeconds,
		                this.pingResponseTimeoutMillis, this.windowIdGenerator));

		return pipeline;
	}

	/**
	 * @return the availableIncomingWindows
	 */
	@ManagedAttribute(description = "Number of available windows for incoming messages. Once these are exhausted, a connected "
	        + "client needs to wait for acknowledgements for all sent messages before being allowed to send further messages.")
	public int getAvailableIncomingWindows() {
		return this.availableIncomingWindows;
	}

	/**
	 * @return the incomingWindowWaitTimeMillis
	 */
	@ManagedAttribute(description = "If no free window is available when processing an incoming message, we wait up to this time "
	        + "span in milliseconds for a window to become available. If this fails, we discard that message.")
	public long getIncomingWindowWaitTimeMillis() {
		return this.incomingWindowWaitTimeMillis;
	}

	/**
	 * @return the failedLoginResponseDelayMillis
	 */
	@ManagedAttribute(description = "To prevent denial of service (DoS) attacks, we delay our response to a failed login attempt for this number of milliseconds.")
	public long getFailedLoginResponseDelayMillis() {
		return this.failedLoginResponseDelayMillis;
	}

	/**
	 * @return the pingIntervalSeconds
	 */
	@ManagedAttribute(description = "The interval in seconds between two pings we send out to clients.")
	public int getPingIntervalSeconds() {
		return this.pingIntervalSeconds;
	}

	/**
	 * @return the pingResponseTimeoutMillis
	 */
	@ManagedAttribute(description = "After sending a ping to a connected client we wait for up to this number of milliseconds "
	        + "before we consider the ping as failed and close this channel.")
	public long getPingResponseTimeoutMillis() {
		return this.pingResponseTimeoutMillis;
	}
}
