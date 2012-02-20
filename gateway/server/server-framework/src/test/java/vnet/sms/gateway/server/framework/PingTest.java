package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.ObjectMessage;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.DenyAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.LocalClient;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class PingTest extends AbstractGatewayServerTest {

	@Test
	public final void assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed()
	        throws Throwable {
		final LocalAddress serverAddress = new LocalAddress("test:server:7");

		final int availableIncomingWindows = 1000;
		final long incomingWindowWaitTimeMillis = 1L;
		final long failedLoginResponseMillis = 100L;
		final int pingIntervalSeconds = 1;
		final long pingResponseTimeoutMillis = 200000L;
		final AuthenticationManager authenticationManager = new AcceptAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
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
		client.connect();
		// Should start ping timeout
		client.login(
		        1,
		        "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		        "whatever");

		client.listen(waitForPing);

		assertTrue("Expected to receive Ping after ping interval of "
		        + pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(pingIntervalSeconds * 1000 + 100,
		                TimeUnit.MILLISECONDS));
		client.disconnect();
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
		        "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
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

		final int availableIncomingWindows = 10;
		final long incomingWindowWaitTimeMillis = 1000L;
		final long failedLoginResponseMillis = 1000L;
		final int pingIntervalSeconds = 100;
		final long pingResponseTimeoutMillis = 2000L;
		final AuthenticationManager authenticationManager = new DenyAllAuthenticationManager();
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> channelPipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGatewayServerDoesNotForwardPingRequestToJmsServer",
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        failedLoginResponseMillis, pingIntervalSeconds,
		        pingResponseTimeoutMillis, authenticationManager, jmsTemplate);

		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer",
		        serverAddress, new DefaultLocalServerChannelFactory(),
		        channelPipelineFactory);
		objectUnderTest.start();

		final PingRequest pingRequest = new PingRequest();

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
