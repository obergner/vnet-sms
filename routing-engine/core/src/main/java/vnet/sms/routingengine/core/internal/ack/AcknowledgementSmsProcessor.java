/**
 * 
 */
package vnet.sms.routingengine.core.internal.ack;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public class AcknowledgementSmsProcessor implements Processor {

	private final Logger	 log	= LoggerFactory.getLogger(getClass());

	private ProducerTemplate	outgoingEndpoints;

	private Endpoint	     outgoingAcksEndpoint;

	/**
	 * @param outgoingEndpoints
	 *            the outgoingEndpoints to set
	 */
	public final void setOutgoingEndpoints(
	        final ProducerTemplate outgoingEndpoints) {
		this.outgoingEndpoints = outgoingEndpoints;
	}

	/**
	 * @param outgoingAcksEndpoint
	 *            the outgoingAcksEndpoint to set
	 */
	public final void setOutgoingAcksEndpoint(
	        final Endpoint outgoingAcksEndpoint) {
		this.outgoingAcksEndpoint = outgoingAcksEndpoint;
	}

	/**
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(final Exchange exchange) throws Exception {
		this.log.debug("Processing exchange [{}] ...", exchange);

		final Sms receivedSms = exchange.getIn().getBody(Sms.class);
		this.log.debug("Received SMS {}", receivedSms);

		this.outgoingEndpoints.asyncSend(this.outgoingAcksEndpoint,
		        new Processor() {
			        @Override
			        public void process(final Exchange newExchange)
			                throws Exception {
				        newExchange.getIn().copyFrom(exchange.getIn());
				        newExchange.getIn().setHeader(Headers.EVENT_TYPE,
				                MessageEventType.RECEIVED_SMS_ACKED.name());
				        AcknowledgementSmsProcessor.this.log
				                .debug("Asynchronously sent ACK [{}] to endpoint [{}]",
				                        newExchange.getIn(),
				                        AcknowledgementSmsProcessor.this.outgoingAcksEndpoint);
			        }
		        });

		this.log.debug("Finished processing exchange [{}]", exchange);
	}
}
