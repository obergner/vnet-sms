package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;
import vnet.sms.common.wme.jmsbridge.Headers;
import vnet.sms.gateway.server.framework.test.IntegrationTestClient;
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
public class SendOutgoingMoSmsIT {

	@Autowired
	private IntegrationTestClient	testClient;

	@Autowired
	private JmsTemplate	          jmsClient;

	@Value("#{ '${gateway.server.jmsserver.queues.outgoingMoSms}' }")
	private String	              outgoingMoSmsQueueName;

	public void disconnectTestClient() throws Throwable {
		this.testClient.disconnect();
	}

	@Test
	public final void assertThatGatewayServerSendsMoSmsReceivedViaJmsToConnectedClient()
	        throws Throwable {
		final Sms moSms = new Sms(
		        "assertThatGatewayServerSendsMoSmsReceivedViaJmsToConnectedClient");

		this.testClient.connect();
		final MessageEventPredicate moSmsReceived = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				if (!(e.getMessage() instanceof ReferenceableMessageContainer)) {
					return false;
				}
				return ReferenceableMessageContainer.class.cast(e.getMessage())
				        .getMessage().equals(moSms);
			}
		};
		final CountDownLatch moSmsReceivedByClient = this.testClient
		        .listen(moSmsReceived);

		this.jmsClient.send(this.outgoingMoSmsQueueName, new MessageCreator() {
			@Override
			public Message createMessage(final Session session)
			        throws JMSException {
				final ObjectMessage moSmsObjectMessage = session
				        .createObjectMessage(moSms);
				moSmsObjectMessage.setStringProperty(Headers.EVENT_TYPE,
				        MessageType.SEND_SMS.name());
				return moSmsObjectMessage;
			}
		});

		assertTrue("GatewayServer should have sent MO SMS " + moSms
		        + " received via JMS to connected client",
		        moSmsReceivedByClient.await(2000L, TimeUnit.MILLISECONDS));
	}
}
