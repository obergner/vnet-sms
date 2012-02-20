package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.jms.ObjectMessage;

import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.LocalClient;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class IncomingWindowingTest extends AbstractGatewayServerTest {

	@Test
	public final void assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer()
	        throws Throwable {
		final LocalAddress serverAddress = new LocalAddress(
		        "test:server:assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer");
		final int availableIncomingWindows = 101;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LocalClient client = new LocalClient(serverAddress);
		// Should start ping timeout
		client.connect();

		// Login. Otherwise, our LoginResponse will be discarded
		client.login(
		        1,
		        "assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer",
		        "whatever");
		// Discard forwarded LoginRequest
		jmsTemplate.receive();

		// Login request uses up one window
		for (int i = 2; i <= availableIncomingWindows; i++) {
			final Sms sms = new Sms(
			        "assertThatGatewayServerForwardsUpToWindowSizeManySmsToJmsServer-"
			                + i);
			client.sendMessage(i, sms);
		}
		client.disconnect();

		for (int i = 2; i <= availableIncomingWindows; i++) {
			final ObjectMessage forwardedMessage = (ObjectMessage) jmsTemplate
			        .receive();
			assertNotNull("GatewayServer should have forwarded " + (i - 1)
			        + "th SMS passed in to JMS server, yet it didn't",
			        forwardedMessage);
			assertEquals(
			        "GatewayServer should have forwarded SMS passed in to JMS server",
			        Sms.class, forwardedMessage.getObject().getClass());
		}

		objectUnderTest.stop();
	}

	@SuppressWarnings("serial")
	private static final class TestGatewayServerDescription extends
	        GatewayServerDescription {

		public TestGatewayServerDescription() {
			super("Test", 1, 0, 0, "BETA", 15);
		}
	}

	@Test
	public final void assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer()
	        throws Throwable {
		final LocalAddress serverAddress = new LocalAddress(
		        "test:server:assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer");

		final int availableIncomingWindows = 101;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final LocalClient client = new LocalClient(serverAddress);
		// Should start ping timeout
		client.connect();

		// Login. Otherwise, our LoginResponse will be discarded
		client.login(
		        1,
		        "assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer",
		        "whatever");
		// Discard forwarded LoginRequest
		jmsTemplate.receive();

		// Login request uses up one window
		for (int i = 2; i <= availableIncomingWindows; i++) {
			final Sms sms = new Sms(
			        "assertThatGatewayServerDoesNotForwardMoreThanWindowSizeManySmsToJmsServer-"
			                + i);
			client.sendMessage(i, sms);
		}
		client.disconnect();

		for (int i = 2; i <= availableIncomingWindows; i++) {
			jmsTemplate.receive();
		}

		final ObjectMessage forwardedMessage = (ObjectMessage) jmsTemplate
		        .receive();
		assertNull("GatewayServer should NOT have forwarded more that "
		        + availableIncomingWindows
		        + " messages to JMS server, yet it did", forwardedMessage);

		objectUnderTest.stop();
	}
}
