/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.jmsbridge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.wme.acknowledge.SendSmsAckContainer;
import vnet.sms.common.wme.acknowledge.SendSmsNackContainer;
import vnet.sms.common.wme.send.SendSmsContainer;
import vnet.sms.gateway.nettysupport.publish.outgoing.OutgoingMessagesSender;
import vnet.sms.gateway.server.framework.Jmx;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesSendingJmsMessageListener<ID extends Serializable> {

	private static final MetricName	         JMS_MESSAGE_PROPAGATED_METRIC_NAME	= new MetricName(
	                                                                                    Jmx.GROUP,
	                                                                                    "OutgoingJmsMessages",
	                                                                                    "jms-message-propagated");

	private final Logger	                 log	                            = LoggerFactory
	                                                                                    .getLogger(getClass());

	private final OutgoingMessagesSender<ID>	outgoingMessagesSender;

	private final MetricsRegistry	         metricsRegistry;

	private final Timer	                     jmsMessagePropagatedTimer;

	/**
	 * @param outgoingMessagesSender
	 */
	public OutgoingMessagesSendingJmsMessageListener(
	        final OutgoingMessagesSender<ID> outgoingMessagesSender,
	        final MetricsRegistry metricsRegistry) {
		notNull(outgoingMessagesSender,
		        "Argument 'outgoingMessagesSender' must not be null");
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.outgoingMessagesSender = outgoingMessagesSender;
		this.metricsRegistry = metricsRegistry;
		this.jmsMessagePropagatedTimer = this.metricsRegistry.newTimer(
		        JMS_MESSAGE_PROPAGATED_METRIC_NAME, TimeUnit.MILLISECONDS,
		        TimeUnit.SECONDS);
	}

	/**
	 * @param outgoingMessage
	 * @throws Exception
	 */
	public void handleMessage(final Object outgoingMessage) throws Exception {
		this.log.debug("Preparing to send message {} ...", outgoingMessage);
		final TimerContext duration = this.jmsMessagePropagatedTimer.time();
		try {
			if (outgoingMessage instanceof SendSmsContainer) {
				this.outgoingMessagesSender.sendSms(SendSmsContainer.class
				        .cast(outgoingMessage));
			} else if (outgoingMessage instanceof SendSmsAckContainer) {
				this.outgoingMessagesSender
				        .ackReceivedSms(SendSmsAckContainer.class
				                .cast(outgoingMessage));
			} else if (outgoingMessage instanceof SendSmsNackContainer) {
				this.outgoingMessagesSender
				        .nackReceivedSms(SendSmsNackContainer.class
				                .cast(outgoingMessage));
			} else {
				throw new IllegalArgumentException("Unsupported message type ["
				        + outgoingMessage.getClass().getName()
				        + "] in message [" + outgoingMessage + "]");
			}
		} finally {
			duration.stop();
		}
		this.log.debug("Successfully sent message {}", outgoingMessage);
	}

	@PreDestroy
	public void close() {
		this.log.info("Closing ...");
		this.outgoingMessagesSender.close();
		this.metricsRegistry.removeMetric(JMS_MESSAGE_PROPAGATED_METRIC_NAME);
		this.log.info("Closed");
	}
}
