package vnet.sms.routingengine.core.internal.ack;

import org.apache.camel.Exchange;
import org.junit.Test;

import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;
import vnet.sms.routingengine.core.internal.CamelBlueprintTestSupport;

public class AcknowledgementProcessingContextIT extends
        CamelBlueprintTestSupport {

	@Override
	protected String getBlueprintDescriptor() {
		return "OSGI-INF/blueprint/routing-engine-core.xml";
	}

	@Test
	public void assertThatRoutingEngineRespondsWithAnAckMessageContainingReceivedSmsWhenReceivingAnMtSms()
	        throws Exception {
		final Sms expectedSms = new Sms(
		        "assertThatRoutingEngineRespondsWithAnAckMessageWhenReceivingAnMtSms");
		template().sendBody("jms:queue:Q1", expectedSms);

		final Exchange receivedAck = consumer().receive("jms:queue:Q2");

		final String eventTypeHeader = receivedAck.getIn().getHeader(
		        Headers.EVENT_TYPE, String.class);
		assertEquals(
		        "Expected AcknowledgementSmsProcessor to wrap the received SMS in an Ack message "
		                + "and send it back, yet the exchange received does not carry the appropriate event type header",
		        MessageEventType.RECEIVED_SMS_ACKED,
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
