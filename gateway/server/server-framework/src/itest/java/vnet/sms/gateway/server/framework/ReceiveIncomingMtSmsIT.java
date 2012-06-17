package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.server.framework.test.ForwardingJmsMessageListener;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
import vnet.sms.gateway.server.framework.test.JmsMessagePredicate;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration({
        "classpath:META-INF/services/gateway-server-application-context.xml",
        "classpath:META-INF/services/gateway-server-authentication-manager-context.xml",
        "classpath:META-INF/services/gateway-server-jms-client-context.xml",
        "classpath:META-INF/services/gateway-server-shell-context.xml",
        "classpath*:META-INF/module/module-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-embedded-activemq-broker-context.xml",
        "classpath:META-INF/itest/itest-serialization-transport-plugin-context.xml",
        "classpath:META-INF/itest/itest-test-client-context.xml",
        "classpath:META-INF/itest/itest-test-jms-listener-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-description-context.xml" })
public class ReceiveIncomingMtSmsIT {

	@Autowired
	private IntegrationTestClient	     testClient;

	@Autowired
	private ForwardingJmsMessageListener	incomingMessagesListener;

	@Test
	public final void assertThatGatewayServerForwardsReceivedMtSmsToJmsServer()
	        throws Throwable {
		this.testClient.connect();

		final int loginReference = 1;
		final String username = "assertThatGatewayServerForwardsReceivedMtSmsToJmsServer";
		final LoginRequest successfulLoginRequest = new LoginRequest(username,
		        "whatever");

		this.testClient.sendMessageAndWaitForResponse(loginReference,
		        successfulLoginRequest);

		final int smsReference = 2;
		final Sms mtSms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatGatewayServerForwardsReceivedMtSmsToJmsServer");

		final JmsMessagePredicate isExpectedSms = new JmsMessagePredicate() {
			@Override
			public boolean evaluate(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return false;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();

					return (payload instanceof Sms);
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};
		final CountDownLatch expectedSmsDelivered = this.incomingMessagesListener
		        .awaitMatchingMessage(isExpectedSms);

		this.testClient.sendMessage(smsReference, mtSms);

		this.testClient.disconnect();

		assertTrue("GatewayServer should have forwarded MT SMS to JMS server",
		        expectedSmsDelivered.await(2, TimeUnit.SECONDS));
	}
}
