package vnet.sms.routingengine.core.internal.ack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath:META-INF/module/activemq-module.xml",
        "classpath:META-INF/itest/itest-embedded-activemq-broker-context.xml",
        "classpath:META-INF/itest/itest-activemq-camel.xml" })
public class AckProcessingContextIT {

	@EndpointInject(uri = "jms:queue:QUEUE.T1000.INCOMING_MT_SMS")
	private ProducerTemplate	incomingMtSms;

	@EndpointInject(uri = "jms:queue:QUEUE.T1000.OUTGOING_MT_SMS_ACK")
	private ConsumerTemplate	outgoingMtSmsAcks;

	@Test
	public void assertThatRoutingEngineRespondsWithAnAckMessageContainingReceivedSmsWhenReceivingAnMtSms()
	        throws Exception {
		final Sms expectedSms = new Sms(new Msisdn("01587756444"), new Msisdn(
		        "01587756455"),
		        "assertThatRoutingEngineRespondsWithAnAckMessageWhenReceivingAnMtSms");
		this.incomingMtSms.sendBody("jms:queue:QUEUE.T1000.INCOMING_MT_SMS",
		        expectedSms);

		final Exchange receivedAck = this.outgoingMtSmsAcks
		        .receive("jms:queue:QUEUE.T1000.OUTGOING_MT_SMS_ACK");

		final String eventTypeHeader = receivedAck.getIn().getHeader(
		        Headers.EVENT_TYPE, String.class);
		assertEquals(
		        "Expected AcknowledgementSmsProcessor to wrap the received SMS in an Ack message "
		                + "and send it back, yet the exchange received does not carry the appropriate event type header",
		        MessageEventType.SEND_SMS_ACK,
		        MessageEventType.valueOf(eventTypeHeader));

		final Sms actualReceivedSms = receivedAck.getIn().getBody(Sms.class);
		assertNotNull(
		        "Expected AcknowledgementSmsProcessor to wrap the received SMS in an Ack message "
		                + "and send it back, yet the exchange received does not contain an SMS",
		        actualReceivedSms);
		assertEquals(
		        "Expected AcknowledgementSmsProcessor to wrap the received SMS in an Ack message "
		                + "and send it back, yet the exchange contains an SMS that is different from the on sent",
		        expectedSms.getId(), actualReceivedSms.getId());
	}
}
