package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.server.framework.dummy.DummyAuthenticationProvider;
import vnet.sms.gateway.server.framework.test.ForwardingJmsMessageListener;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
import vnet.sms.gateway.server.framework.test.JmsMessagePredicate;
import vnet.sms.gateway.server.framework.test.MessageEventPredicate;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({
        "classpath:META-INF/services/gateway-server-application-context.xml",
        "classpath:META-INF/services/gateway-server-authentication-manager-context.xml",
        "classpath:META-INF/services/gateway-server-jms-client-context.xml",
        "classpath*:META-INF/module/module-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-embedded-activemq-broker-context.xml",
        "classpath:META-INF/itest/itest-serialization-transport-plugin-context.xml",
        "classpath:META-INF/itest/itest-test-client-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-description-context.xml" })
public class GatewayServerIT {

	@Autowired
	private IntegrationTestClient	                              testClient;

	@Autowired
	private GatewayServer<Integer, ReferenceableMessageContainer>	gatewayServer;

	@Autowired
	private ForwardingJmsMessageListener	                      incomingMessagesListener;

	@Value("#{ '${gateway.server.failedLoginResponseDelayMillis}' }")
	private long	                                              failedLoginResponseMillis;

	@Value("#{ '${gateway.server.pingIntervalSeconds}' }")
	private long	                                              pingIntervalSeconds;

	@Test
	public final void assertThatGatewayServerIsInStateRUNNINGAfterApplicationContextLoad()
	        throws Exception {
		assertEquals(
		        "Initialization of ApplicationContext should started GatewayServer",
		        ServerStatus.RUNNING, this.gatewayServer.getCurrentStatus());
	}

	@Test
	public final void assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int messageReference = 1;
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever", new InetSocketAddress(2048), new InetSocketAddress(
		                65000));

		this.testClient.connect();
		final ReferenceableMessageContainer response = this.testClient
		        .sendMessageAndWaitForResponse(messageReference,
		                successfulLoginRequest);
		this.testClient.disconnect();

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
		final int messageReference = 1;
		final LoginRequest failedLoginRequest = new LoginRequest(
		        "assertThatGatewayServerRespondsWithAFailedLoginResponseToAFailedLoginRequest",
		        DummyAuthenticationProvider.REJECTED_PASSWORD,
		        new InetSocketAddress(2048), new InetSocketAddress(65000));

		this.testClient.connect();
		final ReferenceableMessageContainer response = this.testClient
		        .sendMessageAndWaitForResponse(messageReference,
		                failedLoginRequest);
		this.testClient.disconnect();

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
		final String username = "assertThatGatewayServerForwardsSuccessfulLoginRequestToJmsServer";
		final LoginRequest successfulLoginRequest = new LoginRequest(username,
		        "whatever", new InetSocketAddress(2048), new InetSocketAddress(
		                65000));

		final JmsMessagePredicate isExpectedLoginRequest = new JmsMessagePredicate() {
			@Override
			public boolean evaluate(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return false;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();
					if (!(payload instanceof LoginRequest)) {
						return false;
					}
					final LoginRequest loginRequest = LoginRequest.class
					        .cast(payload);
					if (!loginRequest.getUsername().equals(username)) {
						return false;
					}
					return true;
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};

		final CountDownLatch expectedLoginRequestDelivered = this.incomingMessagesListener
		        .waitForMessage(isExpectedLoginRequest);

		this.testClient.connect();
		this.testClient.sendMessageAndWaitForResponse(messageReference,
		        successfulLoginRequest);
		this.testClient.disconnect();

		assertTrue(
		        "GatewayServer should have forwarded successful LoginRequest to JMS server",
		        expectedLoginRequestDelivered.await(2, TimeUnit.SECONDS));
	}

	@Test
	public final void assertThatGatewayServerForwardsFailedLoginRequestToJmsServer()
	        throws Throwable {
		final int messageReference = 1;
		final String username = "assertThatGatewayServerForwardsFailedLoginRequestToJmsServer";
		final LoginRequest failedLoginRequest = new LoginRequest(username,
		        DummyAuthenticationProvider.REJECTED_PASSWORD,
		        new InetSocketAddress(2048), new InetSocketAddress(65000));

		final JmsMessagePredicate isExpectedLoginRequest = new JmsMessagePredicate() {
			@Override
			public boolean evaluate(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return false;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();
					if (!(payload instanceof LoginRequest)) {
						return false;
					}
					final LoginRequest loginRequest = LoginRequest.class
					        .cast(payload);
					if (!loginRequest.getUsername().equals(username)) {
						return false;
					}
					return true;
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};

		final CountDownLatch expectedLoginRequestDelivered = this.incomingMessagesListener
		        .waitForMessage(isExpectedLoginRequest);

		this.testClient.connect();
		this.testClient.sendMessageAndWaitForResponse(messageReference,
		        failedLoginRequest);
		this.testClient.disconnect();

		assertTrue(
		        "GatewayServer should have forwarded failed LoginRequest to JMS server",
		        expectedLoginRequestDelivered.await(2, TimeUnit.SECONDS));
	}

	@Test
	public final void assertThatGatewayServerDelaysResponseToFailedLoginRequest()
	        throws Throwable {
		final int messageReference = 78;
		final String username = "assertThatGatewayServerDelaysResponseToFailedLoginRequest";
		final LoginRequest failedLoginRequest = new LoginRequest(username,
		        DummyAuthenticationProvider.REJECTED_PASSWORD,
		        new InetSocketAddress(2048), new InetSocketAddress(65000));

		this.testClient.connect();
		final long before = System.currentTimeMillis();
		this.testClient.sendMessageAndWaitForResponse(messageReference,
		        failedLoginRequest);
		final long after = System.currentTimeMillis();
		this.testClient.disconnect();

		assertTrue(
		        "GatewayServer should have delayed response to failed LoginRequest for at least "
		                + this.failedLoginResponseMillis + " ms", after
		                - before >= this.failedLoginResponseMillis);
	}

	@Test
	public final void assertThatGatewayServerRejectsNonLoginMessagesOnAnUnauthenticatedChannel()
	        throws Throwable {
		final int messageReference = 78;
		final PingRequest nonLoginMessage = new PingRequest(
		        new InetSocketAddress(2048), new InetSocketAddress(65000));

		this.testClient.connect();
		final ReferenceableMessageContainer nonLoginResponse = this.testClient
		        .sendMessageAndWaitForResponse(messageReference,
		                nonLoginMessage);
		this.testClient.disconnect();

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
		final MessageEventPredicate matchesPingEvent = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				return ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest;
			}
		};
		this.testClient.connect();

		// Login starts ping timeout
		this.testClient
		        .login(1,
		                "assertThatGatewayServerSendsFirstPingRequestToClientAfterPingIntervalHasElapsed",
		                "whatever");

		final CountDownLatch pingReceived = this.testClient
		        .listen(matchesPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));
		this.testClient.disconnect();
	}

	@Test
	public final void assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse()
	        throws Throwable {
		final MessageEventPredicate matchesReceivedPingEvent = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				return ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage() instanceof PingRequest;
			}
		};
		this.testClient.connect();

		// Login starts ping timeout
		this.testClient
		        .login(1,
		                "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		                "whatever");

		final CountDownLatch pingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        pingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));
		this.testClient.disconnect();

		// Should start ping timeout
		this.testClient.connect();

		// Login. Otherwise, our LoginResponse will be discarded
		this.testClient
		        .login(1,
		                "assertThatGatewayServerContinuesSendingPingRequestsAfterReceivingPingResponse",
		                "whatever");

		final CountDownLatch firstPingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		assertTrue("Expected to receive Ping after ping interval of "
		        + this.pingIntervalSeconds + " seconds had expired",
		        firstPingReceived.await(this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));

		// Send ping response
		final CountDownLatch secondPingReceived = this.testClient
		        .listen(matchesReceivedPingEvent);
		this.testClient.sendMessage(2, PingResponse.accept(new PingRequest(
		        new InetSocketAddress(2000), new InetSocketAddress(3000))));

		assertTrue(
		        "Expected to receive  second Ping after sending PingResponse and aiting for "
		                + this.pingIntervalSeconds + " seconds",
		        secondPingReceived.await(
		                this.pingIntervalSeconds * 1000 + 2000,
		                TimeUnit.MILLISECONDS));

		this.testClient.disconnect();
	}
}
