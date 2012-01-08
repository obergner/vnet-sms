package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.gateway.server.framework.dummy.DummyAuthenticationProvider;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
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
        "classpath:META-INF/itest/itest-test-client-context.xml" })
public class GatewayServerIT {

	@Autowired
	private IntegrationTestClient	                              testClient;

	@Autowired
	private GatewayServer<Integer, ReferenceableMessageContainer>	gatewayServer;

	@Autowired
	private JmsTemplate	                                          jmsServerConnection;

	@Test
	public final void assertThatGatewayServerIsInStateRUNNINGAfterApplicationContextLoad()
	        throws Exception {
		assertEquals(
		        "Initialization of ApplicationContext should started GatewayServer",
		        "RUNNING", this.gatewayServer.getCurrentState().getName());
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

		final CountDownLatch expectedLoginRequestDelivered = new CountDownLatch(
		        1);
		final SimpleMessageListenerContainer jmsListenerContainer = new SimpleMessageListenerContainer();
		jmsListenerContainer.setConnectionFactory(this.jmsServerConnection
		        .getConnectionFactory());
		jmsListenerContainer.setDestination(this.jmsServerConnection
		        .getDefaultDestination());
		final MessageListener expectedLoginRequestListener = new MessageListener() {
			@Override
			public void onMessage(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();
					if (!(payload instanceof LoginRequest)) {
						return;
					}
					final LoginRequest loginRequest = LoginRequest.class
					        .cast(payload);
					if (loginRequest.getUsername().equals(username)) {
						expectedLoginRequestDelivered.countDown();
					}
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};
		jmsListenerContainer.setMessageListener(expectedLoginRequestListener);
		jmsListenerContainer.afterPropertiesSet();
		jmsListenerContainer.start();

		this.testClient.connect();
		this.testClient.sendMessageAndWaitForResponse(messageReference,
		        successfulLoginRequest);
		this.testClient.disconnect();

		assertTrue(
		        "GatewayServer should have forwarded LoginRequest passed in to JMS server",
		        expectedLoginRequestDelivered.await(2, TimeUnit.SECONDS));
		jmsListenerContainer.stop();
	}
}
