package vnet.sms.gateway.server.framework;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.ObjectMessage;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.jmsbridge.WindowedMessageEventToJmsMessageConverter;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.server.framework.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.jmsbridge.MessageForwardingJmsBridge;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.DenyAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.LocalClient;
import vnet.sms.gateway.server.framework.test.SerialIntegersMessageReferenceGenerator;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockConnectionFactory;

public class GatewayServerTest {

	private static final String	DEFAULT_QUEUE_NAME	= "queue.test.defaultDestination";

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullInstanceId() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(null,
		        new LocalAddress("assertThatConstructorRejectsNullInstanceId"),
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsEmptyInstanceId() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "",
		        new LocalAddress("assertThatConstructorRejectsEmptyInstanceId"),
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullSocketAddress() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullSocketAddress", null,
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullServerChannelFactory() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullServerChannelFactory",
		        new LocalAddress(
		                "assertThatConstructorRejectsNullServerChannelFactory"),
		        null, createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullServerChannelPipelineFactory() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullServerChannelFactory",
		        new LocalAddress(
		                "assertThatConstructorRejectsNullServerChannelFactory"),
		        new DefaultLocalServerChannelFactory(), null);
	}

	@Test
	public final void assertThatStartPromotesGatewayServerToStateRunning()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatStartPromotesGatewayServerToStateRunning",
		        new LocalAddress(
		                "assertThatStartPromotesGatewayServerToStateRunning"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		objectUnderTest.start();

		assertEquals(
		        "start() did not promote GatewayServer into state RUNNING",
		        "RUNNING", objectUnderTest.getCurrentState().getName());

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
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry,
		        new MessageForwardingJmsBridge<Integer>(jmsTemplate),
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        authenticationManager, failedLoginResponseMillis,
		        new SerialIntegersMessageReferenceGenerator(),
		        pingIntervalSeconds, pingResponseTimeoutMillis,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test
	public final void assertThatStopPromotesGatewayServerToStateStopped()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatStopPromotesGatewayServerToStateStopped",
		        new LocalAddress(
		                "assertThatStopPromotesGatewayServerToStateStopped"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		objectUnderTest.start();
		objectUnderTest.stop();

		assertEquals("stop() did not promote GatewayServer into state STOPPED",
		        "STOPPED", objectUnderTest.getCurrentState().getName());
	}

	@Test
	public final void assertThatGetChannelMonitorRegistryDoesNotReturnNull() {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGetChannelMonitorRegistryDoesNotReturnNull",
		        new LocalAddress(
		                "assertThatGetChannelMonitorRegistryDoesNotReturnNull"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		assertNotNull("getChannelMonitorRegistry() returned null",
		        objectUnderTest.getChannelMonitorRegistry());
	}

	@Test
	public final void assertThatGetCurrentStateInitiallyReturnsStateSTOPPED() {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        10, 2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatStopPromotesGatewayServerToStateStopped",
		        new LocalAddress(
		                "assertThatStopPromotesGatewayServerToStateStopped"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		assertEquals("getCurrentState() did not return STOPPED after creation",
		        "STOPPED", objectUnderTest.getCurrentState().getName());
	}

	@Test
	public final void assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int messageReference = 1;
		final LocalAddress serverAddress = new LocalAddress("test:server:1");
		final LocalAddress clientAddress = new LocalAddress("test:client:1");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever", clientAddress, serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		final ReferenceableMessageContainer response = client
		        .sendMessageAndWaitForResponse(messageReference,
		                successfulLoginRequest);
		client.disconnect();
		objectUnderTest.stop();

		assertEquals(
		        "GatewayServer should have returned messageReference of LoginRequest passed in",
		        messageReference, response.getMessageReference());
		assertEquals("GatewayServer should have returned LoginResponse",
		        LoginResponse.class, response.getMessage().getClass());
		assertTrue(
		        "GatewayServer should have returned a SUCCESSFUL LoginResponse",
		        LoginResponse.class.cast(response.getMessage())
		                .loginSucceeded());
	}

	@Test
	public final void assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest()
	        throws Throwable {
		final int messageReference = 78;
		final LocalAddress serverAddress = new LocalAddress("test:server:2");
		final LocalAddress clientAddress = new LocalAddress("test:client:2");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever", clientAddress, serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		final ReferenceableMessageContainer response = client
		        .sendMessageAndWaitForResponse(messageReference,
		                failedLoginRequest);
		client.disconnect();
		objectUnderTest.stop();

		assertEquals(
		        "GatewayServer should have returned messageReference of LoginRequest passed in",
		        messageReference, response.getMessageReference());
		assertEquals("GatewayServer should have returned LoginResponse",
		        LoginResponse.class, response.getMessage().getClass());
		assertFalse(
		        "GatewayServer should have returned a FAILED LoginResponse",
		        LoginResponse.class.cast(response.getMessage())
		                .loginSucceeded());
	}

	@Test
	public final void assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer()
	        throws Throwable {
		final int messageReference = 1;
		final LocalAddress serverAddress = new LocalAddress("test:server:3");
		final LocalAddress clientAddress = new LocalAddress("test:client:3");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer",
		        "whatever", clientAddress, serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		client.sendMessage(messageReference, successfulLoginRequest);
		client.disconnect();
		final ObjectMessage forwardedMessage = (ObjectMessage) jmsTemplate
		        .receive();
		objectUnderTest.stop();

		assertNotNull(
		        "GatewayServer should have forwarded LoginRequest passed in to JMS server",
		        forwardedMessage);
		assertEquals(
		        "GatewayServer should have forwarded LoginRequest passed in to JMS server",
		        successfulLoginRequest, forwardedMessage.getObject());
	}

	@Test
	public final void assertThatGatewayServerForwardsFailedLoginRequestToJmsServer()
	        throws Throwable {
		final int messageReference = 1;
		final LocalAddress serverAddress = new LocalAddress("test:server:4");
		final LocalAddress clientAddress = new LocalAddress("test:client:4");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        "whatever", clientAddress, serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		client.sendMessage(messageReference, failedLoginRequest);
		client.disconnect();
		final ObjectMessage forwardedMessage = (ObjectMessage) jmsTemplate
		        .receive();
		objectUnderTest.stop();

		assertNotNull(
		        "GatewayServer should have forwarded LoginRequest passed in to JMS server",
		        forwardedMessage);
		assertEquals(
		        "GatewayServer should have forwarded LoginRequest passed in to JMS server",
		        failedLoginRequest, forwardedMessage.getObject());
	}

	@Test
	public final void assertThatGatewayServerDelaysResponseToFailedLoginRequest()
	        throws Throwable {
		final int messageReference = 78;
		final LocalAddress serverAddress = new LocalAddress("test:server:5");
		final LocalAddress clientAddress = new LocalAddress("test:client:5");

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1000;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever", clientAddress, serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		final long before = System.currentTimeMillis();
		client.sendMessageAndWaitForResponse(messageReference,
		        failedLoginRequest);
		final long after = System.currentTimeMillis();
		client.disconnect();
		objectUnderTest.stop();

		assertTrue(
		        "GatewayServer should have delayed response to failed LoginRequest for at least "
		                + failedLoginResponseMillis + " ms",
		        after - before >= failedLoginResponseMillis);
	}

	@Test
	public final void assertThatGatewayServerRejectsNonLoginMessagesOnAnUnauthenticatedChannel()
	        throws Throwable {
		final int messageReference = 78;
		final LocalAddress serverAddress = new LocalAddress("test:server:6");
		final LocalAddress clientAddress = new LocalAddress("test:client:6");

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1000;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final PingRequest nonLoginMessage = new PingRequest(clientAddress,
		        serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		final ReferenceableMessageContainer nonLoginResponse = client
		        .sendMessageAndWaitForResponse(messageReference,
		                nonLoginMessage);
		client.disconnect();
		objectUnderTest.stop();

		assertEquals(
		        "Expected channel pipeline to send (failed) PingResponse to client after failed login, yet it sent a different reply",
		        PingResponse.class, nonLoginResponse.getMessage().getClass());
		assertFalse(
		        "Expected channel pipeline to send FAILED PingResponse to client after failed login, yet it sent a SUCCESSFUL PingResponse",
		        PingResponse.class.cast(nonLoginResponse.getMessage())
		                .pingSucceeded());
	}

	@Test
	public final void assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed()
	        throws Throwable {
		final LocalAddress serverAddress = new LocalAddress("test:server:7");

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final CountDownLatch pingReceived = new CountDownLatch(1);
		final LocalClient.MessageListener waitForPing = new LocalClient.MessageListener() {

			@Override
			public void messageReceived(final MessageEvent e) {
				if (ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest) {
					pingReceived.countDown();
				}
			}
		};
		final LocalClient client = new LocalClient(serverAddress);
		// Should start ping timeout
		client.connect();
		client.listen(waitForPing);
		assertTrue("Expected to receive Ping after ping interval of "
		        + pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(pingIntervalSeconds * 1000 + 100,
		                TimeUnit.MILLISECONDS));
		client.disconnect();
		objectUnderTest.stop();
	}

	@Test
	public final void assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse()
	        throws Throwable {
		final LocalAddress serverAddress = new LocalAddress("test:server:8");

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LocalClient client = new LocalClient(serverAddress);
		// Should start ping timeout
		client.connect();

		// Login. Otherwise, our LoginResponse will be discarded
		client.login(
		        1,
		        "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		        "whatever");

		final CountDownLatch firstPingReceived = new CountDownLatch(1);
		final AtomicReference<PingRequest> firstReceivedPing = new AtomicReference<PingRequest>();
		final LocalClient.MessageListener waitForFirstPing = new LocalClient.MessageListener() {

			@Override
			public void messageReceived(final MessageEvent e) {
				if (ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest) {
					firstReceivedPing
					        .set((PingRequest) ReferenceableMessageContainer.class
					                .cast(e.getMessage()).getMessage());
					firstPingReceived.countDown();
				}
			}
		};
		client.listen(waitForFirstPing);
		assertTrue("Expected to receive Ping after ping interval of "
		        + pingIntervalSeconds + " seconds had expired",
		        firstPingReceived.await(pingIntervalSeconds * 1000 + 100,
		                TimeUnit.MILLISECONDS));

		final CountDownLatch secondPingReceived = new CountDownLatch(1);
		final LocalClient.MessageListener waitForSecondPing = new LocalClient.MessageListener() {

			@Override
			public void messageReceived(final MessageEvent e) {
				if (ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest) {
					secondPingReceived.countDown();
				}
			}
		};
		client.listen(waitForSecondPing);
		final PingResponse responseToFirstPingRequest = PingResponse
		        .accept(firstReceivedPing.get());
		client.sendMessage(2, responseToFirstPingRequest);
		assertTrue(
		        "Expected to receive  second Ping after sending PingResponse and aiting for "
		                + pingIntervalSeconds + " seconds",
		        secondPingReceived.await(pingIntervalSeconds * 1000 + 100,
		                TimeUnit.MILLISECONDS));

		client.disconnect();
		objectUnderTest.stop();
	}

	@Test
	public final void assertThatGatewayServerDoesNotForwardPingRequestToJmsServer()
	        throws Throwable {
		final int messageReference = 1;
		final LocalAddress serverAddress = new LocalAddress("test:server:9");
		final LocalAddress clientAddress = new LocalAddress("test:client:9");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final PingRequest pingRequest = new PingRequest(clientAddress,
		        serverAddress);

		final LocalClient client = new LocalClient(serverAddress);
		client.connect();
		client.sendMessage(messageReference, pingRequest);
		client.disconnect();
		final ObjectMessage forwardedMessage = (ObjectMessage) jmsTemplate
		        .receive();
		objectUnderTest.stop();

		assertNull(
		        "GatewayServer should NOT have forwarded PingRequest passed in to JMS server, yet it did",
		        forwardedMessage);
	}
}
