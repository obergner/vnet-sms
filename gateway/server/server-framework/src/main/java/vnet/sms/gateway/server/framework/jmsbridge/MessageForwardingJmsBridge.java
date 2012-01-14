/**
 * 
 */
package vnet.sms.gateway.server.framework.jmsbridge;

import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.TimeUnit;

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

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerMetric;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = "vnet.sms.gateway.server.framework:component=JMSBridge")
public class MessageForwardingJmsBridge<ID extends java.io.Serializable>
        implements IncomingMessagesListener<ID> {

	private final Logger	      log	                          = LoggerFactory
	                                                                      .getLogger(getClass());

	private final MeterMetric	  numberOfForwardedSms	          = Metrics
	                                                                      .newMeter(
	                                                                              getClass(),
	                                                                              "number-of-forwarded-sms",
	                                                                              "sms-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final MeterMetric	  numberOfForwardedLoginRequests	= Metrics
	                                                                      .newMeter(
	                                                                              getClass(),
	                                                                              "number-of-forwarded-login-requests",
	                                                                              "login-request-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final MeterMetric	  numberOfForwardedLoginResponses	= Metrics
	                                                                      .newMeter(
	                                                                              getClass(),
	                                                                              "number-of-forwarded-login-responses",
	                                                                              "login-response-forwarded",
	                                                                              TimeUnit.SECONDS);

	private final HistogramMetric	forwardedMessages	          = Metrics
	                                                                      .newHistogram(
	                                                                              getClass(),
	                                                                              "forwarded-messages");

	private final TimerMetric	  sendDuration	                  = Metrics
	                                                                      .newTimer(
	                                                                              getClass(),
	                                                                              "send-duration",
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

	// Number of forwarded SMS

	@ManagedAttribute(description = "COUNT: Aggregate number of all SMS that have been forwarded by this bridge")
	public long getTotalNumberOfForwardedSms() {
		return this.numberOfForwardedSms.count();
	}

	@ManagedAttribute(description = "COUNT: Number of SMS forwarded within last minute")
	public double getNumberOfSmsForwardedWithinLast1Minute() {
		return this.numberOfForwardedSms.oneMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of SMS forwarded within last five minutes")
	public double getNumberOfSmsForwardedWithinLast5Minutes() {
		return this.numberOfForwardedSms.fiveMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of SMS forwarded within last fifteen minutes")
	public double getNumberOfSmsForwardedWithinLast15Minutes() {
		return this.numberOfForwardedSms.fifteenMinuteRate();
	}

	// Number of forwarded login requests

	@ManagedAttribute(description = "COUNT: Aggregate number of all Login Requests that have been forwarded by this bridge")
	public long getTotalNumberOfForwardedLoginRequests() {
		return this.numberOfForwardedLoginRequests.count();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Requests forwarded within last minute")
	public double getNumberOfLoginRequestsForwardedWithinLast1Minute() {
		return this.numberOfForwardedLoginRequests.oneMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Requests forwarded within last five minutes")
	public double getNumberOfLoginRequestsForwardedWithinLast5Minutes() {
		return this.numberOfForwardedLoginRequests.fiveMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Requests forwarded within last fifteen minutes")
	public double getNumberOfLoginRequestsForwardedWithinLast15Minutes() {
		return this.numberOfForwardedLoginRequests.fifteenMinuteRate();
	}

	// Number of forwarded login responses

	@ManagedAttribute(description = "COUNT: Aggregate number of Login Responses that have been forwarded by this bridge")
	public long getTotalNumberOfForwardedLoginResponses() {
		return this.numberOfForwardedLoginResponses.count();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Responses forwarded within last minute")
	public double getNumberOfLoginResponsesForwardedWithinLast1Minute() {
		return this.numberOfForwardedLoginResponses.oneMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Responses forwarded within last five minutes")
	public double getNumberOfLoginResponsesForwardedWithinLast5Minutes() {
		return this.numberOfForwardedLoginResponses.fiveMinuteRate();
	}

	@ManagedAttribute(description = "COUNT: Number of Login Responses forwarded within last fifteen minutes")
	public double getNumberOfLoginResponsesForwardedWithinLast15Minutes() {
		return this.numberOfForwardedLoginResponses.fifteenMinuteRate();
	}

	// Aggregate number of forwarded messages

	@ManagedAttribute(description = "COUNT: Aggregate number of all messages that have been forwarded by this bridge")
	public long getTotalNumberOfForwardedMessages() {
		return getTotalNumberOfForwardedSms()
		        + getTotalNumberOfForwardedLoginRequests()
		        + getTotalNumberOfForwardedLoginResponses();
	}

	// Send durations

	@ManagedAttribute(description = "DURATION: Mean send duration per message in milliseconds")
	public double getMeanSendDurationInMillis() {
		return this.sendDuration.mean();
	}

	@ManagedAttribute(description = "DURATION: Maximum send duration in milliseconds")
	public double getMaxSendDurationInMillis() {
		return this.sendDuration.max();
	}

	@ManagedAttribute(description = "DURATION: Minimum send duration in milliseconds")
	public double getMinSendDurationInMillis() {
		return this.sendDuration.min();
	}

	@ManagedAttribute(description = "DURATION: Standard deviation of all send durations in milliseconds")
	public double getSendDurationStdDevInMillis() {
		return this.sendDuration.stdDev();
	}

	// Throughput

	@ManagedAttribute(description = "TRHOUGHPUT: Mean number of messages forwarded per second")
	public double getMeanNumberOfForwardedMessagesPerSecond() {
		return this.sendDuration.meanRate();
	}

	@ManagedAttribute(description = "THROUGHPUT: Mean number of messages forwarded per second, restricted to those forwarded within the last minute")
	public double getMeanNumberOfForwardedMessagesPerSecondWithinLast1Minute() {
		return this.sendDuration.oneMinuteRate();
	}

	@ManagedAttribute(description = "THROUGHPUT: Mean number of messages forwarded per second, restricted to those forwarded within the last five minutes")
	public double getMeanNumberOfForwardedMessagesPerSecondWithinLast5Minutes() {
		return this.sendDuration.fiveMinuteRate();
	}

	@ManagedAttribute(description = "THROUGHPUT: Mean number of messages forwarded per second, restricted to those forwarded within the last fifteen minutes")
	public double getMeanNumberOfForwardedMessagesPerSecondWithinLast15Minutes() {
		return this.sendDuration.fifteenMinuteRate();
	}
}
