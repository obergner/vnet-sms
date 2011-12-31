/**
 * 
 */
package vnet.sms.gateway.server.framework.jmsbridge;

import static org.apache.commons.lang.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener;

/**
 * @author obergner
 * 
 */
public class MessageForwardingJmsBridge<ID extends java.io.Serializable>
        implements IncomingMessagesListener<ID> {

	private final Logger	  log	= LoggerFactory.getLogger(getClass());

	private final JmsTemplate	jmsTemplate;

	/**
	 * @param jmsTemplate
	 */
	public MessageForwardingJmsBridge(final JmsTemplate jmsTemplate) {
		notNull(jmsTemplate, "Argument 'jmsTemplate' must not be null");
		this.jmsTemplate = jmsTemplate;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#smsReceived(vnet.sms.common.wme.SmsReceivedEvent)
	 */
	@Override
	public void smsReceived(final SmsReceivedEvent<ID> smsReceived) {
		doForward(smsReceived);
	}

	private void doForward(
	        final WindowedMessageEvent<ID, ? extends Message> windowedMessageEvent)
	        throws JmsException {
		this.log.debug("Forwarding {} to [{}] ...", windowedMessageEvent,
		        this.jmsTemplate.getDefaultDestinationName());
		this.jmsTemplate.convertAndSend(windowedMessageEvent);
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
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#loginResponseReceived(vnet.sms.common.wme.LoginResponseReceivedEvent)
	 */
	@Override
	public void loginResponseReceived(
	        final LoginResponseReceivedEvent<ID> loginResponseReceived) {
		doForward(loginResponseReceived);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#pingRequestReceived(vnet.sms.common.wme.PingRequestReceivedEvent)
	 */
	@Override
	public void pingRequestReceived(
	        final PingRequestReceivedEvent<ID> pingRequestReceived) {
		doForward(pingRequestReceived);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.publish.incoming.IncomingMessagesListener#pingResponseReceived(vnet.sms.common.wme.PingResponseReceivedEvent)
	 */
	@Override
	public void pingResponseReceived(
	        final PingResponseReceivedEvent<ID> pingResponseReceived) {
		doForward(pingResponseReceived);
	}
}
