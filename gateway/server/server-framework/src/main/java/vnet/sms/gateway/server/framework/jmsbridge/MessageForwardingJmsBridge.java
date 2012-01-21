/**
 * 
 */
package vnet.sms.gateway.server.framework.jmsbridge;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.TimeUnit;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener;
import vnet.sms.gateway.server.framework.Jmx;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.TimerMetric;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = MessageForwardingJmsBridge.OBJECT_NAME)
public class MessageForwardingJmsBridge<ID extends java.io.Serializable>
        implements IncomingMessagesListener<ID> {

	private static final String	  TYPE	                          = "JMSBridge";

	private static final String	  NAME	                          = "DEFAULT";

	static final String	          OBJECT_NAME	                  = Jmx.GROUP
	                                                                      + ":type="
	                                                                      + TYPE
	                                                                      + ",name="
	                                                                      + NAME;

	private final Logger	      log	                          = LoggerFactory
	                                                                      .getLogger(getClass());

	private final MeterMetric	  numberOfForwardedSms	          = Metrics
	                                                                      .newMeter(
	                                                                              new MetricName(
	                                                                                      Jmx.GROUP,
	                                                                                      TYPE,
	                                                                                      "number-of-forwarded-sms"),
	                                                                              "sms-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final MeterMetric	  numberOfForwardedLoginRequests	= Metrics
	                                                                      .newMeter(
	                                                                              new MetricName(
	                                                                                      Jmx.GROUP,
	                                                                                      TYPE,
	                                                                                      "number-of-forwarded-login-requests"),
	                                                                              "login-request-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final MeterMetric	  numberOfForwardedLoginResponses	= Metrics
	                                                                      .newMeter(
	                                                                              new MetricName(
	                                                                                      Jmx.GROUP,
	                                                                                      TYPE,
	                                                                                      "number-of-forwarded-login-responses"),
	                                                                              "login-response-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final HistogramMetric	forwardedMessages	          = Metrics
	                                                                      .newHistogram(new MetricName(
	                                                                              Jmx.GROUP,
	                                                                              TYPE,
	                                                                              "forwarded-messages-distribution"));

	private final TimerMetric	  sendDuration	                  = Metrics
	                                                                      .newTimer(
	                                                                              new MetricName(
	                                                                                      Jmx.GROUP,
	                                                                                      TYPE,
	                                                                                      "message-send-duration"),
	                                                                              TimeUnit.MILLISECONDS,
	                                                                              TimeUnit.SECONDS);

	private final JmsTemplate	  jmsTemplate;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * @param jmsTemplate
	 */
	public MessageForwardingJmsBridge(final JmsTemplate jmsTemplate) {
		notNull(jmsTemplate, "Argument 'jmsTemplate' must not be null");
		this.jmsTemplate = jmsTemplate;
	}

	// ------------------------------------------------------------------------
	// IncomingMessagesListener
	// ------------------------------------------------------------------------

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#smsReceived(vnet.sms.common.wme.SmsReceivedEvent)
	 */
	@Override
	public void smsReceived(final SmsReceivedEvent<ID> smsReceived) {
		doForward(smsReceived);
		this.numberOfForwardedSms.mark();
	}

	private void doForward(
	        final WindowedMessageEvent<ID, ? extends Message> windowedMessageEvent)
	        throws JmsException {
		this.log.debug("Forwarding {} to [{}] ...", windowedMessageEvent,
		        this.jmsTemplate.getDefaultDestinationName());
		final long before = System.currentTimeMillis();
		this.jmsTemplate.convertAndSend(windowedMessageEvent);
		this.sendDuration.update(System.currentTimeMillis() - before,
		        TimeUnit.MILLISECONDS);
		this.forwardedMessages.update(1);
		this.log.debug("Forwarded {} to [{}]", windowedMessageEvent,
		        this.jmsTemplate.getDefaultDestinationName());
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#loginRequestReceived(vnet.sms.common.wme.LoginRequestReceivedEvent)
	 */
	@Override
	public void loginRequestReceived(
	        final LoginRequestReceivedEvent<ID> loginRequestReceived) {
		doForward(loginRequestReceived);
		this.numberOfForwardedLoginRequests.mark();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#loginResponseReceived(vnet.sms.common.wme.LoginResponseReceivedEvent)
	 */
	@Override
	public void loginResponseReceived(
	        final LoginResponseReceivedEvent<ID> loginResponseReceived) {
		doForward(loginResponseReceived);
		this.numberOfForwardedLoginResponses.mark();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#pingRequestReceived(vnet.sms.common.wme.PingRequestReceivedEvent)
	 */
	@Override
	public void pingRequestReceived(
	        final PingRequestReceivedEvent<ID> pingRequestReceived) {
		// Ignore
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#pingResponseReceived(vnet.sms.common.wme.PingResponseReceivedEvent)
	 */
	@Override
	public void pingResponseReceived(
	        final PingResponseReceivedEvent<ID> pingResponseReceived) {
		// Ignore
	}

	// ------------------------------------------------------------------------
	// JMX API
	// ------------------------------------------------------------------------

	@ManagedAttribute(description = "The name of the JMS queue this JMS bridge will forward messages to by default")
	public String getDefaultDestination() {
		return this.jmsTemplate.getDefaultDestinationName();
	}

	@ManagedAttribute(description = "If this bridge uses the configured settings for delivery mode, "
	        + "message priority and time-to-live, or if these are set administratively")
	public boolean isExplicitQosEnabled() {
		return this.jmsTemplate.isExplicitQosEnabled();
	}

	@ManagedAttribute(description = "The delivery mode to use for each forwarded message")
	public String getDeliveryMode() {
		final int deliveryMode = this.jmsTemplate.getDeliveryMode();
		final String deliveryModeString;
		switch (deliveryMode) {
		case DeliveryMode.PERSISTENT:
			deliveryModeString = "PERSISTENT";
			break;
		case DeliveryMode.NON_PERSISTENT:
			deliveryModeString = "NON_PERSISTENT";
			break;
		default:
			deliveryModeString = "ERROR:UNKNOWN";
		}
		return deliveryModeString;
	}

	@ManagedAttribute(description = "The priority this bridge uses for each message when forwarding it")
	public int getMessagePriority() {
		return this.jmsTemplate.getPriority();
	}

	@ManagedAttribute(description = "The time-to-live in milliseconds of each message sent")
	public long getTimeToLive() {
		return this.jmsTemplate.getTimeToLive();
	}

	@ManagedAttribute(description = "Are we forwarding messages transactionally?")
	public boolean isSessionTransacted() {
		return this.jmsTemplate.isSessionTransacted();
	}

	@ManagedAttribute(description = "The acknowledgement mode used when forwarding messages via JMS")
	public String getSessionAcknowledgeMode() {
		final int ackMode = this.jmsTemplate.getSessionAcknowledgeMode();
		final String ackModeString;
		switch (ackMode) {
		case Session.AUTO_ACKNOWLEDGE:
			ackModeString = "AUTO_ACKNOWLEDGE";
			break;
		case Session.CLIENT_ACKNOWLEDGE:
			ackModeString = "CLIENT_ACKNOWLEDGE";
			break;
		case Session.DUPS_OK_ACKNOWLEDGE:
			ackModeString = "DUPS_OK_ACKNOWLEDGE";
			break;
		case Session.SESSION_TRANSACTED:
			ackModeString = "SESSION_TRANSACTED";
			break;
		default:
			ackModeString = "ERROR:UNKNOWN";
		}
		return ackModeString;
	}

	@ManagedAttribute(description = "The time span in milliseconds after which this bridge will consider a receive failed")
	public long getReceiveTimeoutMillis() {
		return this.jmsTemplate.getReceiveTimeout();
	}
}
