package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.ObjectMessage;

import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.server.framework.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.DenyAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.LocalClient;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class LoginTest extends AbstractGatewayServerTest {

	@Test
	public final void assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int messageReference = 1;
		final LocalAddress serverAddress = new LocalAddress("test:server:1");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever");

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

	@SuppressWarnings("serial")
	private static final class TestGatewayServerDescription extends
	        GatewayServerDescription {

		public TestGatewayServerDescription() {
			super("Test", 1, 0, 0, "BETA", 15);
		}
	}

	@Test
	public final void assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest()
	        throws Throwable {
		final int messageReference = 78;
		final LocalAddress serverAddress = new LocalAddress("test:server:2");

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever");

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

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer",
		        "whatever");

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

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        "whatever");

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

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1000;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerDelaysResponseToFailedLoginRequest",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        "whatever");

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

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1000;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerRejectsNonLoginMessagesOnAnUnauthenticatedChannel",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final PingRequest nonLoginMessage = new PingRequest();

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
}
