package vnet.sms.gateway.server.framework.channel;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.gateway.nettysupport.monitor.incoming.InitialChannelEventsMonitor;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactoryBuilder;
import vnet.sms.gateway.server.framework.internal.jmsbridge.IncomingMessagesForwardingJmsBridge;
import vnet.sms.gateway.transport.spi.DefaultTransportProtocolPlugin;
import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.yammer.metrics.Metrics;

public class GatewayServerChannelPipelineFactoryBuilderTest {

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectThrowsIllegalStateExceptionIfAfterPropertiesSetHasNotBeenCalledBefore()
	        throws Exception {
		final GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu>();
		objectUnderTest.getObject();
	}

	@Test
	public final void assertThatGetObjectTypeReturnsTypeCompatibleWithGatewayServerChannelPipelineFactory() {
		final GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu>();
		final Class<?> objectType = objectUnderTest.getObjectType();

		assertTrue(
		        "getObjectType() should have returned a type that is compatible with "
		                + GatewayServerChannelPipelineFactory.class
		                + ", yet it didn't",
		        GatewayServerChannelPipelineFactory.class
		                .isAssignableFrom(objectType));
	}

	@Test
	public final void assertThatIsSingletonReturnsTrue() {
		final GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, GsmPdu>();

		assertTrue(
		        "isSingleton() should have returned true since a GatewayServerChannelPipelineFactory is a singleton",
		        objectUnderTest.isSingleton());
	}

	@Test
	public final void assertThatAfterPropertiesSetProperlyConstructsAGatewayServerChannelPipelineFactoryIfBuilderIsCorrectlyConfigured()
	        throws Exception {
		final TransportProtocolPlugin<Integer, ReferenceableMessageContainer> transportProtocolPlugin = new DefaultTransportProtocolPlugin<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        createNiceMock(MessageReferenceGenerator.class));
		final GatewayServerChannelPipelineFactoryBuilder<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, ReferenceableMessageContainer>();
		objectUnderTest.plugin(transportProtocolPlugin);
		objectUnderTest
		        .setGatewayServerInstanceId("assertThatAfterPropertiesSetProperlyConstructsAGatewayServerChannelPipelineFactoryIfBuilderIsCorrectlyConfigured");
		objectUnderTest
		        .setAuthenticationManager(createNiceMock(AuthenticationManager.class));
		objectUnderTest.setAvailableIncomingWindows(10);
		objectUnderTest
		        .setMessageForwardingJmsBridge(createNiceMock(IncomingMessagesForwardingJmsBridge.class));
		objectUnderTest.setFailedLoginResponseDelayMillis(2000L);
		objectUnderTest.setIncomingWindowWaitTimeMillis(1000L);
		objectUnderTest.setMBeanExportOperations(new MBeanExporter());
		objectUnderTest.setPingIntervalSeconds(2);
		objectUnderTest.setPingResponseTimeoutMillis(3000L);
		objectUnderTest
		        .setInitialChannelEventsMonitor(new InitialChannelEventsMonitor());
		objectUnderTest.setAllConnectedChannels(new DefaultChannelGroup());
		objectUnderTest.setMetricsRegistry(Metrics.defaultRegistry());
		objectUnderTest.afterPropertiesSet();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> product = objectUnderTest
		        .getObject();

		assertNotNull(
		        "afterPropertiesSet() should have built a proper GatewayServerChannelPipelineFactory",
		        product);
	}
}
