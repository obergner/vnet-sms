package vnet.sms.gateway.server.framework.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.base64.Base64Encoder;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.nettytest.ChannelEventFilter;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

public class GatewayServerChannelPipelineFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullPduType() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        null,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	private static class SerialIntegersMessageReferenceGenerator implements
	        MessageReferenceGenerator<Integer> {

		private final AtomicInteger	nextReference	= new AtomicInteger();

		@Override
		public Integer nextMessageReference() {
			return this.nextReference.incrementAndGet();
		}
	}

	private static class AcceptAllAuthenticationManager implements
	        AuthenticationManager {

		@Override
		public Authentication authenticate(final Authentication authentication)
		        throws AuthenticationException {
			return new TestingAuthenticationToken(
			        authentication.getPrincipal(),
			        authentication.getCredentials(), "test-role");
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullFrameDecoder() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        null,
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullEncoder() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        null,
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullTransportProtocolAdpatingUpstreamChannelHandler() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        null,
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullTransportProtocolAdaptingDownstreamChannelHandler() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry), null, channelMonitorRegistry,
		        10, 1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullChannelMonitorRegistry() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), null, 10, 1000L,
		        new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullAuthenticationManager() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, null, 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L,
		        ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullMessageReferenceGenerator() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L, null, 2,
		        2000L, ManagementFactory.getPlatformMBeanServer());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullMBeanServer() {
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new DelimiterBasedFrameDecoder(0, ChannelBuffers.EMPTY_BUFFER),
		        new Base64Decoder(),
		        new Base64Encoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry, 10,
		        1000L, new AcceptAllAuthenticationManager(), 1000L,
		        new SerialIntegersMessageReferenceGenerator(), 2, 2000L, null);
	}

	@Test
	public final void assertThatTheProducedPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever", new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, successfulLoginRequest));
		final MessageEvent encodedLoginResponse = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send LoginRequestAcceptedEvent to client after successful login, yet it sent NO message in reply",
		        encodedLoginResponse);
		final Message decodedLoginResponse = deserialize(encodedLoginResponse);
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
	        final ChannelMonitorRegistry channelMonitorRegistry,
	        final AuthenticationManager authenticationManager) {
		return new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(
		                channelMonitorRegistry),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
		                channelMonitorRegistry), channelMonitorRegistry,
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        authenticationManager, failedLoginResponseMillis,
		        new SerialIntegersMessageReferenceGenerator(),
		        pingIntervalSeconds, pingResponseTimeoutMillis,
		        ManagementFactory.getPlatformMBeanServer());
	}

	private ChannelBuffer serialize(final int messageRef, final Message message)
	        throws IOException {
		final ByteArrayOutputStream serializedContainer = new ByteArrayOutputStream();
		final ObjectEncoderOutputStream objectEncoderOut = new ObjectEncoderOutputStream(
		        serializedContainer);
		objectEncoderOut.writeObject(ReferenceableMessageContainer.wrap(
		        messageRef, message));
		objectEncoderOut.flush();
		objectEncoderOut.close();

		return ChannelBuffers.copiedBuffer(serializedContainer.toByteArray());
	}

	private Message deserialize(final MessageEvent messageEvent)
	        throws ClassNotFoundException, IOException {
		if (!(messageEvent.getMessage() instanceof ChannelBuffer)) {
			throw new IllegalArgumentException(
			        "Expected payload of type ChannelBuffer. Got: "
			                + messageEvent.getMessage());
		}
		final ChannelBuffer encodedMessage = ChannelBuffer.class
		        .cast(messageEvent.getMessage());
		final ByteArrayInputStream encodedMessageBytes = new ByteArrayInputStream(
		        encodedMessage.array());
		final ObjectDecoderInputStream objectDecoderIn = new ObjectDecoderInputStream(
		        encodedMessageBytes);
		final Object readObject = objectDecoderIn.readObject();
		if (!(readObject instanceof ReferenceableMessageContainer)) {
			throw new IllegalArgumentException("Expected message of type "
			        + ReferenceableMessageContainer.class.getName()
			        + " after deserialization. Got: " + readObject);
		}

		return Message.class.cast(ReferenceableMessageContainer.class.cast(
		        readObject).getMessage());
	}

	@Test
	public final void assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 200L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever", new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, failedLoginRequest));
		Thread.sleep(failedLoginResponseMillis + 100);
		final MessageEvent encodedLoginResponse = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send LoginRequestRejectedEvent to client after failed login, yet it sent NO message in reply",
		        encodedLoginResponse);
		final Message decodedLoginResponse = deserialize(encodedLoginResponse);
		assertEquals(
		        "Expected channel pipeline to send LoginResponse to client after failed login, yet it sent a different reply",
		        LoginResponse.class, decodedLoginResponse.getClass());
		assertFalse(
		        "Expected channel pipeline to send FAILED LoginResponse to client after failed login, yet it sent a SUCCESSFUL LoginResponse",
		        LoginResponse.class.cast(decodedLoginResponse).loginSucceeded());
	}

	private static class DenyAllAuthenticationManager implements
	        AuthenticationManager {

		@Override
		public Authentication authenticate(final Authentication authentication)
		        throws AuthenticationException {
			throw new BadCredentialsException("Reject all");
		}
	}

	@Test
	public final void assertThatTheProducedPipelineDelaysRespondToAFailedLoginRequest()
	        throws Throwable {
		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 400L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever", new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, failedLoginRequest));

		final MessageEvent noLoginResponseExpectedYet = embeddedPipeline
		        .nextSentMessageEvent();
		assertNull(
		        "Expected channel pipeline to delay response to failed login request for "
		                + failedLoginResponseMillis
		                + " milliseconds, yet it immediately sent a response",
		        noLoginResponseExpectedYet);

		Thread.sleep(failedLoginResponseMillis + 100);
		final MessageEvent expectedLoginResponse = embeddedPipeline
		        .nextSentMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send LoginRequestRejectedEvent to client after "
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
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		final PingRequest nonLoginRequest = new PingRequest(
		        new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, nonLoginRequest));
		final MessageEvent encodedPingResponse = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "Expected channel pipeline to send (failed) PingResponse to client when receiving a PingRequest on an unauthenticated channel, yet it sent NO message in reply",
		        encodedPingResponse);
		final Message decodedPingResponse = deserialize(encodedPingResponse);
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
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		// Will fire "channel connected" and thus start ping interval
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);

		final MessageEvent expectedEncodedPingRequst = embeddedPipeline
		        .nextSentMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send PingRequest to client "
		                + pingIntervalSeconds
		                + " seconds after the channel had been connected, yet it sent NO message",
		        expectedEncodedPingRequst);
		final Message decodedPingRequest = deserialize(expectedEncodedPingRequst);
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
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		// Will fire "channel connected" and thus start ping interval
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		// First, authenticate channel since otherwise we won't accept a
		// PingResponse
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineContinuesSendingPingRequestsAfterReceivingPingResponse",
		        "whatever", new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, successfulLoginRequest));
		// Consume LoginResponse - we don't care about it
		embeddedPipeline.nextSentMessageEvent();

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);
		final MessageEvent expectedInitialPingRequest = embeddedPipeline
		        .nextSentMessageEvent();
		assertNotNull(
		        "Expected channel pipeline to send PingRequest to client "
		                + pingIntervalSeconds
		                + " seconds after the channel had been connected, yet it sent NO message",
		        expectedInitialPingRequest);
		final Message decodedPingRequest = deserialize(expectedInitialPingRequest);
		assertEquals(
		        "Expected channel pipeline to send PingRequest to client after ping interval has expired, yet it sent a different message",
		        PingRequest.class, decodedPingRequest.getClass());

		embeddedPipeline
		        .receive(serialize(2, PingResponse.accept(PingRequest.class
		                .cast(decodedPingRequest))));

		Thread.sleep(pingIntervalSeconds * 1000L + 100L);
		final MessageEvent expectedSecondPingRequest = embeddedPipeline
		        .nextSentMessageEvent();
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
		final ChannelMonitorRegistry channelMonitorRegistry = new ChannelMonitorRegistry();
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newObjectUnderTest(
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, channelMonitorRegistry,
		        authenticationManager);
		// Will fire "channel connected" and thus register our
		// IncomingWindowStore
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);

		// First, authenticate channel since otherwise we won't accept any
		// incoming messages
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatTheProducedPipelineIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable",
		        "whatever", new InetSocketAddress(1), new InetSocketAddress(2));
		embeddedPipeline.receive(serialize(1, successfulLoginRequest));
		// Discard LoginResponse - we don't care
		embeddedPipeline.nextSentMessageEvent();

		// We already consumed one window for our LoginRequest - remember that
		// we don't support freeing windows yet
		for (int i = 0; i < availableIncomingWindows - 1; i++) {
			embeddedPipeline.receive(serialize(i + 2, new PingRequest(
			        new InetSocketAddress(1), new InetSocketAddress(2))));
		}

		embeddedPipeline.receive(serialize(12, new PingRequest(
		        new InetSocketAddress(5), new InetSocketAddress(6))));

		final ChannelEvent propagatedMessageEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
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
