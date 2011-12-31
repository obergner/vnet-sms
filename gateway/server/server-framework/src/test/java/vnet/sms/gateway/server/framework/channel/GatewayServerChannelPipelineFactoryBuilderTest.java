package vnet.sms.gateway.server.framework.channel;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;

import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.server.framework.jmsbridge.MessageForwardingJmsBridge;
import vnet.sms.gateway.server.framework.spi.DefaultTransportProtocolPlugin;
import vnet.sms.gateway.server.framework.spi.TransportProtocolPlugin;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

public class GatewayServerChannelPipelineFactoryBuilderTest {

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectThrowsIllegalStateExceptionIfAfterPropertiesSetHasNotBeenCalledBefore()
	        throws Exception {
		final GatewayServerChannelPipelineFactoryBuilder<Integer, Message> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, Message>();
		objectUnderTest.getObject();
	}

	@Test
	public final void assertThatGetObjectTypeReturnsTypeCompatibleWithGatewayServerChannelPipelineFactory() {
		final GatewayServerChannelPipelineFactoryBuilder<Integer, Message> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, Message>();
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
		final GatewayServerChannelPipelineFactoryBuilder<Integer, Message> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, Message>();

		assertTrue(
		        "isSingleton() should have returned true since a GatewayServerChannelPipelineFactory is a singleton",
		        objectUnderTest.isSingleton());
	}

	@Test
	public final void assertThatAfterPropertiesSetProperlyConstructsAGatewayServerChannelPipelineFactoryIfBuilderIsCorrectlyConfigured()
	        throws Exception {
		final TransportProtocolPlugin<Integer, ReferenceableMessageContainer> transportProtocolPlugin = new DefaultTransportProtocolPlugin<Integer, ReferenceableMessageContainer>(
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                createNiceMock(ChannelMonitorRegistry.class)),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                createNiceMock(ChannelMonitorRegistry.class)));
		final GatewayServerChannelPipelineFactoryBuilder<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerChannelPipelineFactoryBuilder<Integer, ReferenceableMessageContainer>();
		objectUnderTest.plugin(transportProtocolPlugin);
		objectUnderTest
		        .setGatewayServerInstanceId("assertThatAfterPropertiesSetProperlyConstructsAGatewayServerChannelPipelineFactoryIfBuilderIsCorrectlyConfigured");
		objectUnderTest
		        .setAuthenticationManager(createNiceMock(AuthenticationManager.class));
		objectUnderTest.setAvailableIncomingWindows(10);
		objectUnderTest
		        .setChannelMonitorRegistry(createNiceMock(ChannelMonitorRegistry.class));
		objectUnderTest
		        .setMessageForwardingJmsBridge(createNiceMock(MessageForwardingJmsBridge.class));
		objectUnderTest.setFailedLoginResponseDelayMillis(2000L);
		objectUnderTest.setIncomingWindowWaitTimeMillis(1000L);
		objectUnderTest.setMbeanServer(ManagementFactory
		        .getPlatformMBeanServer());
		objectUnderTest.setPduType(ReferenceableMessageContainer.class);
		objectUnderTest.setPingIntervalSeconds(2);
		objectUnderTest.setPingResponseTimeoutMillis(3000L);
		objectUnderTest
		        .setWindowIdGenerator(createNiceMock(MessageReferenceGenerator.class));
		objectUnderTest.afterPropertiesSet();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> product = objectUnderTest
		        .getObject();

		assertNotNull(
		        "afterPropertiesSet() should have built a proper GatewayServerChannelPipelineFactory",
		        product);
	}
}
