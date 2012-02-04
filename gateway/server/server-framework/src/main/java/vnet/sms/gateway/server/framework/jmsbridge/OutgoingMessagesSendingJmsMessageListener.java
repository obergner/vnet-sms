/**
 * 
 */
package vnet.sms.gateway.server.framework.jmsbridge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedContainer;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedContainer;
import vnet.sms.common.wme.send.SendSmsContainer;
import vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesSendingJmsMessageListener<ID extends Serializable> {

	private final Logger	                 log	= LoggerFactory
	                                                     .getLogger(getClass());

	private final OutgoingMessagesSender<ID>	outgoingMessagesSender;

	/**
	 * @param outgoingMessagesSender
	 */
	public OutgoingMessagesSendingJmsMessageListener(
	        final OutgoingMessagesSender<ID> outgoingMessagesSender) {
		notNull(outgoingMessagesSender,
		        "Argument 'outgoingMessagesSender' must not be null");
		this.outgoingMessagesSender = outgoingMessagesSender;
	}

	/**
	 * @param outgoingMessage
	 * @throws Exception
	 */
	public void handleMessage(final Object outgoingMessage) throws Exception {
		this.log.debug("Preparing to send message {} ...", outgoingMessage);
		if (outgoingMessage instanceof SendSmsContainer) {
			this.outgoingMessagesSender.sendSms(SendSmsContainer.class
			        .cast(outgoingMessage));
		} else if (outgoingMessage instanceof ReceivedSmsAckedContainer) {
			this.outgoingMessagesSender
			        .ackReceivedSms(ReceivedSmsAckedContainer.class
			                .cast(outgoingMessage));
		} else if (outgoingMessage instanceof ReceivedSmsNackedContainer) {
			this.outgoingMessagesSender
			        .nackReceivedSms(ReceivedSmsNackedContainer.class
			                .cast(outgoingMessage));
		} else {
			throw new IllegalArgumentException("Unsupported message type ["
			        + outgoingMessage.getClass().getName() + "] in message ["
			        + outgoingMessage + "]");
		}
		this.log.debug("Successfully sent message {}", outgoingMessage);
	}

	@PreDestroy
	public void close() {
		this.log.info("Closing ...");
		this.outgoingMessagesSender.close();
		this.log.info("Closed");
	}
}
