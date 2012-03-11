package vnet.sms.gateway.server.framework;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;
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
        "classpath:META-INF/itest/itest-test-jms-listener-context.xml",
        "classpath:META-INF/itest/itest-gateway-server-description-context.xml" })
public class SendOutgoingAcknowledgementsIT {

	@Autowired
	private IntegrationTestClient	     testClient;

	@Autowired
	private JmsTemplate	                 jmsClient;

	@Autowired
	private ForwardingJmsMessageListener	incomingMessagesListener;

	@Value("#{ '${gateway.server.jmsserver.queues.outgoingMtSmsAck}' }")
	private String	                     outgoingMtSmsAckQueueName;

	@Value("#{ '${gateway.server.jmsserver.queues.outgoingMtSmsNack}' }")
	private String	                     outgoingMtSmsNackQueueName;

	@Before
	public void connectTestClient() throws Throwable {
		this.testClient.connect();
	}

	@After
	public void disconnectTestClient() throws Throwable {
		this.testClient.disconnect();
	}

	@Test(timeout = 2000L)
	public final void assertThatGatewayServerSendsSmsAckReceivedViaJmsToConnectedClient()
	        throws Throwable {
		// 1. Login
		final int loginReference = 1;
		final String username = "assertThatGatewayServerSendsSmsAckReceivedViaJmsToConnectedClient";
		final LoginRequest successfulLoginRequest = new LoginRequest(username,
		        "whatever");
		final ReferenceableMessageContainer loginResponseContainer = this.testClient
		        .sendMessageAndWaitForResponse(loginReference,
		                successfulLoginRequest);
		assertTrue(
		        "PRECONDITION VIOLATED: Login failed",
		        (loginResponseContainer.getMessage() instanceof LoginResponse)
		                && loginResponseContainer.getMessage(
		                        LoginResponse.class).loginSucceeded());

		// 2. Send MT SMS
		final int mtSmsReference = 2;
		final Sms mtSms = new Sms(
		        "assertThatGatewayServerSendsMoSmsReceivedViaJmsToConnectedClient");
		final JmsMessagePredicate isExpectedMtSms = new JmsMessagePredicate() {
			@Override
			public boolean evaluate(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return false;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();
					if (!(payload instanceof Sms)) {
						return false;
					}
					final Sms sms = Sms.class.cast(payload);
					if (!sms.equals(mtSms)) {
						return false;
					}
					return true;
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};
		final Future<Message> forwardedMtSms = this.incomingMessagesListener
		        .receiveMatchingMessage(isExpectedMtSms);
		this.testClient.sendMessage(mtSmsReference, mtSms);

		// 3. Make test client wait for expected MT SMS Ack
		final MessageEventPredicate isExpectedMtSmsAck = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				final vnet.sms.common.messages.Message message = ReferenceableMessageContainer.class
				        .cast(e.getMessage()).getMessage();
				if (!(message instanceof Acknowledgement)) {
					return false;
				}
				return Acknowledgement.class.cast(message).is(
				        Acknowledgement.Status.ACK);
			}
		};
		final CountDownLatch expectedAckReceived = this.testClient
		        .listen(isExpectedMtSmsAck);

		// 4. Create ack for forwarded MT SMS and send it via JMS
		final ObjectMessage forwardedMtSmsMsg = (ObjectMessage) forwardedMtSms
		        .get();
		final Integer smsReference = (Integer) forwardedMtSmsMsg
		        .getObjectProperty(Headers.MESSAGE_REFERENCE);
		final int receivingChannelId = forwardedMtSmsMsg
		        .getIntProperty(Headers.RECEIVING_CHANNEL_ID);
		this.jmsClient.send(this.outgoingMtSmsAckQueueName,
		        new MessageCreator() {
			        @Override
			        public Message createMessage(final Session session)
			                throws JMSException {
				        final ObjectMessage mtSmsAckObjectMessage = session
				                .createObjectMessage(mtSms);
				        mtSmsAckObjectMessage.setStringProperty(
				                Headers.EVENT_TYPE,
				                MessageEventType.RECEIVED_SMS_ACKED.name());
				        mtSmsAckObjectMessage.setIntProperty(
				                Headers.RECEIVING_CHANNEL_ID,
				                receivingChannelId);
				        mtSmsAckObjectMessage.setObjectProperty(
				                Headers.MESSAGE_REFERENCE, smsReference);
				        return mtSmsAckObjectMessage;
			        }
		        });

		assertTrue(
		        "Expected to receive Ack for previously sent MT SMS, yet didn't",
		        expectedAckReceived.await(2000L, TimeUnit.MILLISECONDS));
	}

	@Test(timeout = 2000L)
	public final void assertThatGatewayServerSendsSmsNackReceivedViaJmsToConnectedClient()
	        throws Throwable {
		// 1. Login
		final int loginReference = 1;
		final String username = "assertThatGatewayServerSendsSmsNackReceivedViaJmsToConnectedClient";
		final LoginRequest successfulLoginRequest = new LoginRequest(username,
		        "whatever");
		final ReferenceableMessageContainer loginResponseContainer = this.testClient
		        .sendMessageAndWaitForResponse(loginReference,
		                successfulLoginRequest);
		assertTrue(
		        "PRECONDITION VIOLATED: Login failed",
		        (loginResponseContainer.getMessage() instanceof LoginResponse)
		                && loginResponseContainer.getMessage(
		                        LoginResponse.class).loginSucceeded());

		// 2. Send MT SMS
		final int mtSmsReference = 2;
		final Sms mtSms = new Sms(
		        "assertThatGatewayServerSendsSmsNackReceivedViaJmsToConnectedClient");
		final JmsMessagePredicate isExpectedMtSms = new JmsMessagePredicate() {
			@Override
			public boolean evaluate(final Message msg) {
				try {
					if (!(msg instanceof ObjectMessage)) {
						return false;
					}
					final ObjectMessage omsg = ObjectMessage.class.cast(msg);
					final Object payload = omsg.getObject();
					if (!(payload instanceof Sms)) {
						return false;
					}
					final Sms sms = Sms.class.cast(payload);
					if (!sms.equals(mtSms)) {
						return false;
					}
					return true;
				} catch (final JMSException e) {
					throw new RuntimeException(e);
				}
			}
		};
		final Future<Message> forwardedMtSms = this.incomingMessagesListener
		        .receiveMatchingMessage(isExpectedMtSms);
		this.testClient.sendMessage(mtSmsReference, mtSms);

		// 3. Make test client wait for expected MT SMS Nack
		final MessageEventPredicate isExpectedMtSmsAck = new MessageEventPredicate() {
			@Override
			public boolean evaluate(final MessageEvent e) {
				final vnet.sms.common.messages.Message message = ReferenceableMessageContainer.class
				        .cast(e.getMessage()).getMessage();
				if (!(message instanceof Acknowledgement)) {
					return false;
				}
				return Acknowledgement.class.cast(message).is(
				        Acknowledgement.Status.NACK);
			}
		};
		final CountDownLatch expectedAckReceived = this.testClient
		        .listen(isExpectedMtSmsAck);

		// 4. Create ack for forwarded MT SMS and send it via JMS
		final ObjectMessage forwardedMtSmsMsg = (ObjectMessage) forwardedMtSms
		        .get();
		final Integer smsReference = (Integer) forwardedMtSmsMsg
		        .getObjectProperty(Headers.MESSAGE_REFERENCE);
		final int receivingChannelId = forwardedMtSmsMsg
		        .getIntProperty(Headers.RECEIVING_CHANNEL_ID);
		this.jmsClient.send(this.outgoingMtSmsNackQueueName,
		        new MessageCreator() {
			        @Override
			        public Message createMessage(final Session session)
			                throws JMSException {
				        final ObjectMessage mtSmsNackObjectMessage = session
				                .createObjectMessage(mtSms);
				        mtSmsNackObjectMessage.setStringProperty(
				                Headers.EVENT_TYPE,
				                MessageEventType.RECEIVED_SMS_NACKED.name());
				        mtSmsNackObjectMessage.setIntProperty(
				                Headers.RECEIVING_CHANNEL_ID,
				                receivingChannelId);
				        mtSmsNackObjectMessage.setObjectProperty(
				                Headers.MESSAGE_REFERENCE, smsReference);
				        mtSmsNackObjectMessage.setIntProperty(
				                Headers.ERROR_KEY, 1);
				        mtSmsNackObjectMessage
				                .setStringProperty(Headers.ERROR_DESCRIPTION,
				                        "assertThatGatewayServerSendsSmsNackReceivedViaJmsToConnectedClient");
				        return mtSmsNackObjectMessage;
			        }
		        });

		assertTrue(
		        "Expected to receive Ack for previously sent MT SMS, yet didn't",
		        expectedAckReceived.await(2000L, TimeUnit.MILLISECONDS));
	}
}
