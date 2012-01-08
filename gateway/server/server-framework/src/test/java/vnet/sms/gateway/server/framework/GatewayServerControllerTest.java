package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.wme.jmsbridge.WindowedMessageEventToJmsMessageConverter;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.server.framework.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.jmsbridge.MessageForwardingJmsBridge;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.SerialIntegersMessageReferenceGenerator;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockConnectionFactory;

public class GatewayServerControllerTest {

	private static final String	DEFAULT_QUEUE_NAME	= "queue.test.defaultDestination";

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullGatewayServer() {
		new GatewayServerController<Integer, Object>(null);
	}

	@Test
	public final void assertThatStartPromotesGatewayServerToStateRunning()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> gatewayServer = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatStartPromotesGatewayServerToStateRunning",
		        new LocalAddress(
		                "GatewayServerControllerTest::assertThatStartPromotesGatewayServerToStateRunning"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);
		final GatewayServerController<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerController<Integer, ReferenceableMessageContainer>(
		        gatewayServer);

		objectUnderTest.start();

		assertEquals(
		        "start() did not promote GatewayServer into state RUNNING",
		        ServerStatus.RUNNING, objectUnderTest.getCurrentStatus());

		objectUnderTest.stop();
	}

	private final JmsTemplate newJmsTemplate() {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);

		final ConfigurationManager configurationManager = new ConfigurationManager();

		final MockConnectionFactory mockConnectionFactory = new MockConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnectionFactory);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);

		return jmsTemplate;
	}

	private GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> newGatewayServerChannelPipelineFactory(
	        final int availableIncomingWindows,
	        final long incomingWindowWaitTimeMillis,
	        final long failedLoginResponseMillis,
	        final int pingIntervalSeconds,
	        final long pingResponseTimeoutMillis,
	        final AuthenticationManager authenticationManager,
	        final JmsTemplate jmsTemplate) {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		return new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "newObjectUnderTest",
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        channelMonitorRegistry,
		        new MessageForwardingJmsBridge<Integer>(jmsTemplate),
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        authenticationManager, failedLoginResponseMillis,
		        new SerialIntegersMessageReferenceGenerator(),
		        pingIntervalSeconds, pingResponseTimeoutMillis,
		        new MBeanExporter());
	}

	@Test
	public final void assertThatStopPromotesGatewayServerToStateStopped()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> gatewayServer = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatStopPromotesGatewayServerToStateStopped",
		        new LocalAddress(
		                "GatewayServerControllerTest::assertThatStopPromotesGatewayServerToStateStopped"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		final GatewayServerController<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerController<Integer, ReferenceableMessageContainer>(
		        gatewayServer);

		objectUnderTest.start();
		objectUnderTest.stop();

		assertEquals("stop() did not promote GatewayServer into state STOPPED",
		        ServerStatus.STOPPED, objectUnderTest.getCurrentStatus());
	}

	@Test
	public final void assertThatGetChannelMonitorRegistryDoesNotReturnNull() {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> gatewayServer = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGetChannelMonitorRegistryDoesNotReturnNull",
		        new LocalAddress(
		                "GatewayServerControllerTest::assertThatGetChannelMonitorRegistryDoesNotReturnNull"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		final GatewayServerController<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerController<Integer, ReferenceableMessageContainer>(
		        gatewayServer);

		assertNotNull("getChannelMonitorRegistry() returned null",
		        objectUnderTest.getChannelMonitorRegistry());
	}

	@Test
	public final void assertThatGetInstanceIdReturnsCorrectInstanceId() {
		final String expectedInstanceId = "assertThatGetInstanceIdReturnsCorrectInstanceId";

		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> gatewayServer = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        expectedInstanceId,
		        new LocalAddress(
		                "GatewayServerControllerTest::assertThatGetChannelMonitorRegistryDoesNotReturnNull"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		final GatewayServerController<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerController<Integer, ReferenceableMessageContainer>(
		        gatewayServer);

		assertEquals("getInstanceId() returned wrong ID", expectedInstanceId,
		        objectUnderTest.getInstanceId());
	}

	@Test
	public final void assertThatGetLocalAddressReturnsCorrectAddress() {
		final LocalAddress expectedLocalAddress = new LocalAddress(
		        "GatewayServerControllerTest::assertThatGetLocalAddressReturnsCorrectAddress");

		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> gatewayServer = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGetLocalAddressReturnsCorrectAddress",
		        expectedLocalAddress, new DefaultLocalServerChannelFactory(),
		        pipelineFactory);

		final GatewayServerController<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServerController<Integer, ReferenceableMessageContainer>(
		        gatewayServer);

		assertEquals("getLocalAddress() returned wrong listen address",
		        expectedLocalAddress, objectUnderTest.getLocalAddress());
	}
}
