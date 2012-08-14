package vnet.sms.routingengine.core.internal.ack;

import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

public class AckProcessingContextIT extends CamelSpringTestSupport {

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
		        "META-INF/module/activemq-module.xml",
		        "META-INF/itest/itest-embedded-activemq-broker-context.xml",
		        "META-INF/itest/itest-activemq-camel.xml");
	}

	@Test
	public void assertThatRoutingEngineRespondsWithAnAckMessageContainingReceivedSmsWhenReceivingAnMtSms()
	        throws Exception {
		final Sms expectedSms = new Sms(new Msisdn("01587756444"), new Msisdn(
		        "01587756455"),
		        "assertThatRoutingEngineRespondsWithAnAckMessageWhenReceivingAnMtSms");
		template().sendBody("jms:queue:QUEUE.T1000.INCOMING_MT_SMS",
		        expectedSms);

		final Exchange receivedAck = consumer().receive(
		        "jms:queue:QUEUE.T1000.OUTGOING_MT_SMS_ACK");

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
