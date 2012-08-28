package vnet.sms.gateway.server.framework.channel;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;

import javax.management.Notification;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.base64.Base64Encoder;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.util.HashedWheelTimer;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.monitor.incoming.InitialChannelEventsMonitor;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettytest.embedded.ChannelEventFilters;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.internal.jmsbridge.IncomingMessagesForwardingJmsBridge;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.DenyAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.SerialIntegersMessageReferenceGenerator;
import vnet.sms.gateway.server.framework.test.SerializationUtils;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.yammer.metrics.Metrics;

public class GatewayServerChannelPipelineFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullPduType() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullPduType",
		        null,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullFrameDecoder() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullFrameDecoder",
		        ReferenceableMessageContainer.class,
		        null,
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullEncoder() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullEncoder",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        null,
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullTransportProtocolAdpatingUpstreamChannelHandler() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullTransportProtocolAdpatingUpstreamChannelHandler",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        null,
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullTransportProtocolAdaptingDownstreamChannelHandler() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullTransportProtocolAdaptingDownstreamChannelHandler",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        null, new IncomingMessagesForwardingJmsBridge<Integer>(
		                jmsTemplate), 10, 1000L,
		        new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullAuthenticationManager() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullAuthenticationManager",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, null, 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(),
		        new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullMessageReferenceGenerator() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullMessageReferenceGenerator",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L, null,
		        2, 2000L, new MBeanExporter(),
		        new InitialChannelEventsMonitor(), Metrics.defaultRegistry(),
		        new HashedWheelTimer(), new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullMBeanServer() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullMBeanServer",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L, null,
		        new InitialChannelEventsMonitor(), Metrics.defaultRegistry(),
		        new HashedWheelTimer(), new DefaultChannelGroup());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullChannelGroup() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullMBeanServer",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), new InitialChannelEventsMonitor(), Metrics
		                .defaultRegistry(), new HashedWheelTimer(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullInitialChannelEventsMonitor() {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "assertThatConstructorRejectsNullMBeanServer",
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        new MBeanExporter(), null, Metrics.defaultRegistry(),
		        new HashedWheelTimer(), new DefaultChannelGroup());
	}

	@Test
	public final void assertThatTheProducedPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever");
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        successfulLoginRequest));
		final MessageEvent encodedLoginResponse = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send SendLoginRequestAckEvent to client after successful login, yet it sent NO message in reply",
		        encodedLoginResponse);
		final GsmPdu decodedLoginResponse = SerializationUtils
		        .deserialize(encodedLoginResponse);
		assertEquals(
		        "Expected channel pipeline to send LoginResponse to client after successful login, yet it sent a different reply",
		        LoginResponse.class, decodedLoginResponse.getClass());
		assertTrue(
		        "Expected channel pipeline to send SUCCESSFUL LoginResponse to client after successful login, yet it sent a FAILED LoginResponse",
		        LoginResponse.class.cast(decodedLoginResponse).loginSucceeded());
	}

	private GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> newObjectUnderTest(
	        final int availableIncomingWindows,
	        final long incomingWindowWaitTimeMillis,
	        final long failedLoginResponseMillis,
	        final int pingIntervalSeconds,
	        final long pingResponseTimeoutMillis,
	        final AuthenticationManager authenticationManager) {
		final JmsTemplate jmsTemplate = createNiceMock(JmsTemplate.class);
		expect(jmsTemplate.getDefaultDestinationName()).andReturn(
		        "queue.test.defaultDestination");
		replay(jmsTemplate);

		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());

		final NotificationPublisher notPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
			}
		};
		final InitialChannelEventsMonitor initialChannelEventsMonitor = new InitialChannelEventsMonitor(
		        notPublisher);

		return new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "newObjectUnderTest",
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        authenticationManager, failedLoginResponseMillis,
		        new SerialIntegersMessageReferenceGenerator(),
		        pingIntervalSeconds, pingResponseTimeoutMillis, mbeanExporter,
		        initialChannelEventsMonitor, Metrics.defaultRegistry(),
		        new HashedWheelTimer(), new DefaultChannelGroup());
	}

	@Test
	public final void assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 200L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever");
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        failedLoginRequest));
		Thread.sleep(failedLoginResponseMillis + 100);
		final MessageEvent encodedLoginResponse = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send SendLoginRequestNackEvent to client after failed login, yet it sent NO message in reply",
		        encodedLoginResponse);
		final GsmPdu decodedLoginResponse = SerializationUtils
		        .deserialize(encodedLoginResponse);
		assertEquals(
		        "Expected channel pipeline to send LoginResponse to client after failed login, yet it sent a different reply",
		        LoginResponse.class, decodedLoginResponse.getClass());
		assertFalse(
		        "Expected channel pipeline to send FAILED LoginResponse to client after failed login, yet it sent a SUCCESSFUL LoginResponse",
		        LoginResponse.class.cast(decodedLoginResponse).loginSucceeded());
	}

	@Test
	public final void assertThatTheProducedPipelineDelaysResponseToAFailedLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 400L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever");
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        failedLoginRequest));

		final MessageEvent noLoginResponseExpectedYet = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();
		assertNull(
		        "Expected channel pipeline to delay response to failed login request for "
		                + failedLoginResponseMillis
		                + " milliseconds, yet it immediately sent a response",
		        noLoginResponseExpectedYet);

		Thread.sleep(failedLoginResponseMillis + 100);
		final MessageEvent expectedLoginResponse = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send SendLoginRequestNackEvent to client after "
		                + failedLoginResponseMillis
		                + " milliseconds, yet it sent NO message in reply",
		        expectedLoginResponse);
	}

	@Test
	public final void assertThatTheProducedPipelineRejectsNonLoginMessagesOnAnUnauthenticatedChannel()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 200L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		final PingRequest nonLoginRequest = new PingRequest();
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        nonLoginRequest));
		final MessageEvent encodedPingResponse = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send (failed) PingResponse to client when receiving a PingRequest on an unauthenticated channel, yet it sent NO message in reply",
		        encodedPingResponse);
		final GsmPdu decodedPingResponse = SerializationUtils
		        .deserialize(encodedPingResponse);
		assertEquals(
		        "Expected channel pipeline to send (failed) PingResponse to client after failed login, yet it sent a different reply",
		        PingResponse.class, decodedPingResponse.getClass());
		assertFalse(
		        "Expected channel pipeline to send FAILED PingResponse to client after failed login, yet it sent a SUCCESSFUL PingResponse",
		        PingResponse.class.cast(decodedPingResponse).pingSucceeded());
	}

	@Test
	public final void assertThatTheProducedPipelineSendsFirstPingRequestToClientAfterPingIntervalHasElapsed()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 400L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 10000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		// Simulate successful channel authentication => we start to ping after
		// pingIntervalSeconds
		embeddedPipeline
		        .injectUpstreamChannelEvent(new ChannelSuccessfullyAuthenticatedEvent(
		                embeddedPipeline.getChannel(),
		                new LoginRequest(
		                        "assertThatOutgoingPingChannelHandlerSendsPingAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed",
		                        "password")));

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);

		final MessageEvent expectedEncodedPingRequst = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send PingRequest to client "
		                + pingIntervalSeconds
		                + " seconds after the channel had been connected, yet it sent NO message",
		        expectedEncodedPingRequst);
		final GsmPdu decodedPingRequest = SerializationUtils
		        .deserialize(expectedEncodedPingRequst);
		assertEquals(
		        "Expected channel pipeline to send LoginResponse to client after failed login, yet it sent a different reply",
		        PingRequest.class, decodedPingRequest.getClass());
	}

	@Test
	public final void assertThatTheProducedPipelineContinuesSendingPingRequestsAfterReceivingPingResponse()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 400L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 10000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		// Will fire "channel connected" and thus start ping interval
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		// First, authenticate channel since otherwise we won't accept a
		// PingResponse
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineContinuesSendingPingRequestsAfterReceivingPingResponse",
		        "whatever");
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        successfulLoginRequest));
		// Consume LoginResponse - we don't care about it
		embeddedPipeline.downstreamMessageEvents().nextMessageEvent();

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);
		final MessageEvent expectedInitialPingRequest = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send PingRequest to client "
		                + pingIntervalSeconds
		                + " seconds after the channel had been connected, yet it sent NO message",
		        expectedInitialPingRequest);
		final GsmPdu decodedPingRequest = SerializationUtils
		        .deserialize(expectedInitialPingRequest);
		assertEquals(
		        "Expected channel pipeline to send PingRequest to client after ping interval has expired, yet it sent a different message",
		        PingRequest.class, decodedPingRequest.getClass());

		embeddedPipeline
		        .receive(SerializationUtils.serialize(2, PingResponse
		                .accept(PingRequest.class.cast(decodedPingRequest))));

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);
		final MessageEvent expectedSecondPingRequest = embeddedPipeline
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send PingRequest to client "
		                + pingIntervalSeconds
		                + " seconds after receiving a PingResponse, yet it sent NO message",
		        expectedSecondPingRequest);
	}

	@Test
	public final void assertThatTheProducedPipelineIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 400L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 10000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager);
		// Will fire "channel connected" and thus register our
		// IncomingWindowStore
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embeddedPipeline.connectChannel();

		// First, authenticate channel since otherwise we won't accept any
		// incoming messages
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable",
		        "whatever");
		embeddedPipeline.receive(SerializationUtils.serialize(1,
		        successfulLoginRequest));
		// Discard LoginResponse - we don't care
		embeddedPipeline.downstreamMessageEvents().nextMessageEvent();

		for (int i = 0; i < availableIncomingWindows; i++) {
			embeddedPipeline.receive(SerializationUtils.serialize((i + 2),
			        new PingRequest()));
		}

		embeddedPipeline.receive(SerializationUtils.serialize(12,
		        new PingRequest()));

		final ChannelEvent propagatedMessageEvent = embeddedPipeline
		        .upstreamChannelEvents()
		        .nextMatchingChannelEvent(
		                ChannelEventFilters
		                        .ofType(NoWindowForIncomingMessageAvailableEvent.class));

		assertNotNull(
		        "Expected channel pipeline to propagate error event when rejecting incoming message due to no window available",
		        propagatedMessageEvent);
		assertEquals(
		        "Channel pipeline propagated unexpected event when rejecting incoming message due to no window available",
		        NoWindowForIncomingMessageAvailableEvent.class,
		        propagatedMessageEvent.getClass());
	}
}
